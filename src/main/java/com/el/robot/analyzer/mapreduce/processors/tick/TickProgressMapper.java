package com.el.robot.analyzer.mapreduce.processors.tick;

import com.el.betting.common.TeamUtils;
import com.el.betting.sdk.v2.*;import com.el.betting.sdk.v4.*;
import com.el.betting.sdk.v2.betoption.api.BetOption;
import com.el.betting.sdk.v2.betoption.bettype.totalpoints.TotalPointsBetOption;
import com.el.betting.sdk.v3.statistic.EventMinuteStatistic;
import com.el.betting.sdk.v3.statistic.EventPriceStatistic;
import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.util.BetOptionConverter;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;
import com.el.robot.analyzer.mapreduce.util.BetfairDataReader;
import com.el.robot.analyzer.mapreduce.util.converts.TotalPointsBetOptionConverter;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculates tick progress for each minute
 */
public class TickProgressMapper extends Mapper<Object, TextArrayWritable, Text, BSONObject> {

    private final static Logger log = LoggerFactory.getLogger(TickProgressMapper.class);

    private BetOptionConverter betOptionConverter;

    @Override
    public void setup(Context context) throws IOException, InterruptedException {
        betOptionConverter = new BetOptionConverter(new TotalPointsBetOptionConverter());
    }

    @Override
    public void map(Object key, TextArrayWritable values, Context context) throws IOException, InterruptedException {
        List<TotalPointsBetOption> totalPointsBetOptions = new ArrayList<>();
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


            TotalPointsBetOption betOption = (TotalPointsBetOption) betOptionOptional.get();
            if (betOption.getTotalType() == TotalType.UNDER) {
                totalPointsBetOptions.add(betOption);
            }
        }

        Event<Team> event = (Event<Team>) totalPointsBetOptions.get(0).getEvent();
        EventPriceStatistic eventPriceStatistic = new EventPriceStatistic();
        eventPriceStatistic.setStartTime(event.getStartTime());
        eventPriceStatistic.setHomeTeam(TeamUtils.getHomeTeam(event.getParticipants()).getName());
        eventPriceStatistic.setAwayTeam(TeamUtils.getAwayTeam(event.getParticipants()).getName());
        eventPriceStatistic.setLeague(String.valueOf(event.getProperty("league")));
        eventPriceStatistic.setCategory(String.valueOf(event.getProperty("category")));

        //order by matched time
        totalPointsBetOptions = totalPointsBetOptions.stream()
                .sorted((scoreBetOption1, scoreBetOption2) ->
                        ((LocalDateTime) (scoreBetOption1.getProperty("firstTimeMatched")))
                                .compareTo((LocalDateTime) scoreBetOption2.getProperty("firstTimeMatched")))
                .collect(Collectors.toList());

        //fix last time matched, if last time matched is > next first time matched => lastTimeMatched = next firstTimeMatched
        for (int i = 0; i < totalPointsBetOptions.size() - 1; i++) {
            final LocalDateTime lastTimeMatched = totalPointsBetOptions.get(i).getProperty("lastTimeMatched", LocalDateTime.class);
            final LocalDateTime nextFirstTimeMatched = totalPointsBetOptions.get(i + 1).getProperty("firstTimeMatched", LocalDateTime.class);
            if (lastTimeMatched.isAfter(nextFirstTimeMatched)) {
                totalPointsBetOptions.get(i).addProperty("lastTimeMatched", nextFirstTimeMatched);
            }
        }

        //now find the price for each minute
        List<EventMinuteStatistic> eventMinuteStatistics = new ArrayList<>();
        int minute = -10;
        for (TotalPointsBetOption totalPointsBetOption : totalPointsBetOptions) {
            if (!(totalPointsBetOption.getPoints() == 2.5) && totalPointsBetOption.getPeriod() != Period.MATCH) {
                continue;
            }

            final LocalDateTime firstTimeMatched = totalPointsBetOption.getProperty("firstTimeMatched", LocalDateTime.class);
            final LocalDateTime lastTimeMatched = totalPointsBetOption.getProperty("lastTimeMatched", LocalDateTime.class);
            final LocalDateTime marketOpenTime = totalPointsBetOption.getProperty("marketOpenTime", LocalDateTime.class);
            final long firstTimeMatchedMinute = marketOpenTime.until(firstTimeMatched, ChronoUnit.MINUTES);
            final long lastTimeMatchedMinute = marketOpenTime.until(lastTimeMatched, ChronoUnit.MINUTES);
            if (firstTimeMatchedMinute <= minute) {
                if (lastTimeMatchedMinute >= minute) {
                    long pricePeriod = lastTimeMatchedMinute - minute + 1;
                    final BigDecimal volumeMatched = totalPointsBetOption.getProperty("volumeMatched", BigDecimal.class);
                    while (minute <= lastTimeMatchedMinute) {
                        EventMinuteStatistic eventMinuteStatistic = new EventMinuteStatistic();
                        eventMinuteStatistic.setMinute(minute++);
                        eventMinuteStatistic.setPrice(totalPointsBetOption.getPrice().doubleValue());
                        BigDecimal matchedAmount = volumeMatched.divide(BigDecimal.valueOf(pricePeriod), 2, RoundingMode.HALF_UP);
                        eventMinuteStatistic.setMatchedAmount(matchedAmount.doubleValue());
                        eventMinuteStatistics.add(eventMinuteStatistic);
                    }
                } else {
                    continue;
                }
            } else {
                //price is unavailable somehow
            }
        }
        System.out.println(eventMinuteStatistics);
    }
}
