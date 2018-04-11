package com.el.robot.analyzer.mapreduce.processors;

import com.el.betting.common.CollectionUtil;
import com.el.betting.common.TeamUtils;
import com.el.betting.sdk.v2.BetOutcome;
import com.el.betting.sdk.v2.BetType;
import com.el.betting.sdk.v2.Event;
import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v4.*;
import com.el.betting.sdk.v2.betoption.api.BetOption;
import com.el.betting.sdk.v2.betoption.bettype.correctscore.CorrectScoreBetOption;
import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.util.BetOptionConverter;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;
import com.el.robot.analyzer.mapreduce.util.BetfairDataReader;
import com.el.robot.analyzer.mapreduce.util.converts.CorrectScoreBetOptionConverter;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.betting.sdk.v3.statistic.GoalStatistic;
import com.el.betting.sdk.v3.metadata.AccuracyLevel;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.convert.MongoConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class BetOptionStatsMapper extends Mapper<Object, TextArrayWritable, Text, BSONObject> {

    private final static Logger log = LoggerFactory.getLogger(BetOptionStatsMapper.class);

    private BetOptionConverter betOptionConverter;

    private MongoConverter mongoConverter;

    private ClassPathXmlApplicationContext applicationContext;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        betOptionConverter = new BetOptionConverter(new CorrectScoreBetOptionConverter());
        applicationContext = new ClassPathXmlApplicationContext("/mongo-context.xml", BetOptionStatsMapper.class);
        applicationContext.registerShutdownHook();
        mongoConverter = applicationContext.getBean(MongoConverter.class);
    }

    @Override
    public void map(Object key, TextArrayWritable values, Context context) throws IOException, InterruptedException {
        EventStatistic eventStatistic = new EventStatistic();
        List<CorrectScoreBetOption> correctScoreBetOptions = new ArrayList<>();
        for (Writable writable : values.get()) {
            Text text = (Text) writable;
            BetfairDataLine betfairDataLine = BetfairDataReader.readLine(text.toString());
            Optional<BetOption> betOptionOptional;
            try {
                betOptionOptional = betOptionConverter.convertToBetOption(betfairDataLine);
            } catch (IllegalArgumentException e) {
                log.warn(e.getMessage(), e);
                continue;
            }

            if (!betOptionOptional.isPresent() || betOptionOptional.get().getPrice().doubleValue() >= 100) {
                continue;
            }

            CorrectScoreBetOption betOption = (CorrectScoreBetOption) betOptionOptional.get();
            if (betOption.getProperty("betOutcome") == BetOutcome.WON) {
                Event<Team> event = (Event<Team>) betOption.getEvent();
                eventStatistic.setStartTime(event.getStartTime());
                eventStatistic.setHomeTeam(TeamUtils.getHomeTeam(event.getParticipants()).getName());
                eventStatistic.setAwayTeam(TeamUtils.getAwayTeam(event.getParticipants()).getName());
                eventStatistic.setHomeTeamGoalCount(betOption.getHomeSideScore());
                eventStatistic.setAwayTeamGoalCount(betOption.getAwaySideScore());
                eventStatistic.setLeague(String.valueOf(event.getProperty("league")));
                eventStatistic.setCategory(String.valueOf(event.getProperty("category")));
            }
            correctScoreBetOptions.add(betOption);
            context.getCounter(BetType.CORRECT_SCORE).increment(1);
        }

        //group by score and pick only one the last matched bet option for each score
        Map<String, CorrectScoreBetOption> scoreToBetOptionMap = correctScoreBetOptions
                .stream()
                .filter(correctScoreBetOption -> correctScoreBetOption.getHomeSideScore() + correctScoreBetOption.getAwaySideScore() <= eventStatistic.getGoalCount())
                .collect(Collectors.groupingBy(correctScoreBetOption -> (
                        correctScoreBetOption.getHomeSideScore() + "-" + correctScoreBetOption.getAwaySideScore()))
                )
                .entrySet()
                .stream()
                .map(entry -> {
                    CorrectScoreBetOption lastMatchedScopeBetOption = entry.getValue().stream()
                            .max((scoreBetOption1, scoreBetOption2) ->
                                    ((LocalDateTime) (scoreBetOption1.getProperty("lastTimeMatched")))
                                            .compareTo((LocalDateTime) scoreBetOption2.getProperty("lastTimeMatched")))
                            .get();

                    return new AbstractMap.SimpleEntry<>(entry.getKey(), lastMatchedScopeBetOption);
                })
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));


        int homeTeamGoalCount = 0;
        int awayTeamGoalCount = 0;
        while (homeTeamGoalCount + awayTeamGoalCount < eventStatistic.getGoalCount()) {
            String initialScore = homeTeamGoalCount + "-" + awayTeamGoalCount;
            String nextHomeTeamScore = (homeTeamGoalCount + 1) + "-" + awayTeamGoalCount;
            String nextAwayTeamScore = homeTeamGoalCount + "-" + (awayTeamGoalCount + 1);

            CorrectScoreBetOption previousScoreBetOption = scoreToBetOptionMap.get(initialScore);
            CorrectScoreBetOption nextHomeScoreBetOption = scoreToBetOptionMap.get(nextHomeTeamScore);
            CorrectScoreBetOption nextAwayScoreBetOption = scoreToBetOptionMap.get(nextAwayTeamScore);

            if (nextHomeScoreBetOption != null && nextAwayScoreBetOption != null) {
                if (((LocalDateTime) (nextHomeScoreBetOption.getProperty("lastTimeMatched")))
                        .isAfter(((LocalDateTime) (nextAwayScoreBetOption.getProperty("lastTimeMatched"))))) {
                    //next home team scored, because the bet continued after away team score
                    homeTeamGoalCount++;
                    LocalDateTime goalTime = previousScoreBetOption != null &&
                            ((LocalDateTime) (previousScoreBetOption.getProperty("lastTimeMatched")))
                                    .isAfter(((LocalDateTime) (nextAwayScoreBetOption.getProperty("lastTimeMatched"))))
                            ? ((LocalDateTime) (previousScoreBetOption.getProperty("lastTimeMatched")))
                            : ((LocalDateTime) (nextAwayScoreBetOption.getProperty("lastTimeMatched")));

                    LocalDateTime marketOpenTime = CollectionUtil.getFirstNotNull(
                            (LocalDateTime) nextAwayScoreBetOption.getProperty("marketOpenTime"),
                            (LocalDateTime) nextHomeScoreBetOption.getProperty("marketOpenTime"),
                            nextAwayScoreBetOption.getEvent().getStartTime());

                    long goalMinute = marketOpenTime.until(goalTime, ChronoUnit.MINUTES);
                    GoalStatistic goalStatistic = new GoalStatistic();
                    goalStatistic.setGoalTime(goalTime);
                    goalStatistic.setTeamName(eventStatistic.getHomeTeam());
                    goalStatistic.setGoalNumber(homeTeamGoalCount + awayTeamGoalCount);
                    goalStatistic.setTeamGoalNumber(homeTeamGoalCount);
                    goalStatistic.setAccuracy(AccuracyLevel.HIGH);
                    goalStatistic.setGoalMinute((int) goalMinute);
                    eventStatistic.addGoalStatistic(goalStatistic);
                } else { //next away team scored, because the bet continued after away team score
                    awayTeamGoalCount++;
                    LocalDateTime goalTime = previousScoreBetOption != null &&
                            ((LocalDateTime) (previousScoreBetOption.getProperty("lastTimeMatched")))
                                    .isAfter(((LocalDateTime) (nextHomeScoreBetOption.getProperty("lastTimeMatched"))))
                            ? ((LocalDateTime) (previousScoreBetOption.getProperty("lastTimeMatched")))
                            : ((LocalDateTime) (nextHomeScoreBetOption.getProperty("lastTimeMatched")));

                    LocalDateTime marketOpenTime = CollectionUtil.getFirstNotNull(
                            (LocalDateTime) nextHomeScoreBetOption.getProperty("marketOpenTime"),
                            (LocalDateTime) nextAwayScoreBetOption.getProperty("marketOpenTime"),
                            nextAwayScoreBetOption.getEvent().getStartTime());
                    long goalMinute = marketOpenTime.until(goalTime, ChronoUnit.MINUTES);
                    GoalStatistic goalStatistic = new GoalStatistic();
                    goalStatistic.setGoalTime(goalTime);
                    goalStatistic.setTeamName(eventStatistic.getAwayTeam());
                    goalStatistic.setGoalNumber(homeTeamGoalCount + awayTeamGoalCount);
                    goalStatistic.setTeamGoalNumber(awayTeamGoalCount);
                    goalStatistic.setAccuracy(AccuracyLevel.HIGH);
                    goalStatistic.setGoalMinute((int) goalMinute);
                    eventStatistic.addGoalStatistic(goalStatistic);
                }
            } else if (homeTeamGoalCount + 1 > eventStatistic.getHomeTeamGoalCount()
                    && nextAwayScoreBetOption != null
                    && previousScoreBetOption != null) { //next away team scored
                awayTeamGoalCount++;
                LocalDateTime goalTime = ((LocalDateTime) (previousScoreBetOption.getProperty("lastTimeMatched")));
                LocalDateTime marketOpenTime = CollectionUtil.getFirstNotNull(
                        (LocalDateTime) nextAwayScoreBetOption.getProperty("marketOpenTime"),
                        nextAwayScoreBetOption.getEvent().getStartTime());
                long goalMinute = marketOpenTime.until(goalTime, ChronoUnit.MINUTES);
                GoalStatistic goalStatistic = new GoalStatistic();
                goalStatistic.setGoalTime(goalTime);
                goalStatistic.setTeamName(eventStatistic.getAwayTeam());
                goalStatistic.setGoalNumber(homeTeamGoalCount + awayTeamGoalCount);
                goalStatistic.setTeamGoalNumber(awayTeamGoalCount);
                goalStatistic.setAccuracy(AccuracyLevel.LOW);
                goalStatistic.setGoalMinute((int) goalMinute);
                eventStatistic.addGoalStatistic(goalStatistic);
            } else if (awayTeamGoalCount + 1 > eventStatistic.getAwayTeamGoalCount()
                    && nextHomeScoreBetOption != null
                    && previousScoreBetOption != null) {
                //next home team scored
                homeTeamGoalCount++;
                LocalDateTime goalTime = ((LocalDateTime) (previousScoreBetOption.getProperty("lastTimeMatched")));
                LocalDateTime marketOpenTime = CollectionUtil.getFirstNotNull(
                        (LocalDateTime) nextHomeScoreBetOption.getProperty("marketOpenTime"),
                        nextHomeScoreBetOption.getEvent().getStartTime());

                long goalMinute = marketOpenTime.until(goalTime, ChronoUnit.MINUTES);
                GoalStatistic goalStatistic = new GoalStatistic();
                goalStatistic.setGoalTime(goalTime);
                goalStatistic.setTeamName(eventStatistic.getHomeTeam());
                goalStatistic.setGoalNumber(homeTeamGoalCount + awayTeamGoalCount);
                goalStatistic.setTeamGoalNumber(homeTeamGoalCount);
                goalStatistic.setAccuracy(AccuracyLevel.LOW);
                goalStatistic.setGoalMinute((int) goalMinute);
                eventStatistic.addGoalStatistic(goalStatistic);
            } else {
                log.error("Can't find goal information for game " + eventStatistic.getHomeTeam() + " v " + eventStatistic.getAwayTeam());
                eventStatistic.getGoalStatistics().clear();
                break;
            }
        }


        if (eventStatistic.getHomeTeam() != null) {
            DBObject bsonObject = new BasicDBObject();
            mongoConverter.write(eventStatistic, bsonObject);
            Text eventDescription = new Text(eventStatistic.getHomeTeam() + " vs " + eventStatistic.getAwayTeam() + "/" + eventStatistic.getStartTime().toString());
            eventStatistic.setDescription(eventStatistic.getHomeTeam() + " vs " + eventStatistic.getAwayTeam() + "/" + eventStatistic.getStartTime().toString());
            context.write(eventDescription, bsonObject);
        } else {
            Writable writable = values.get()[0];
            Text text = (Text) writable;
            BetfairDataLine betfairDataLine = BetfairDataReader.readLine(text.toString());
            log.error("Not Event<? extends Participant> information found for: " + betfairDataLine.getHomeTeam() + " v " + betfairDataLine.getAwayTeam());
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        super.cleanup(context);
        applicationContext.close();
    }
}
