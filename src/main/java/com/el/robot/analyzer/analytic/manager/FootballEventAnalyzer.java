package com.el.robot.analyzer.analytic.manager;

import com.el.betting.common.TeamUtils;
import com.el.betting.sdk.v2.*;import com.el.betting.sdk.v4.*;
import com.el.betting.sdk.v2.betline.api.LayBackBetLine;
import com.el.betting.sdk.v2.betline.bettype.correctscore.CorrectScoreLayBackBetLine;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.betting.utils.EventUtils;
import com.el.robot.crawler.db.v3.EventStatisticManager;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.el.betting.utils.EventUtils.isAnyOptionAvailable;

/**
 * EventAnalyzer does deeper analyzing also using db and historical data information
 * for returning requested information.
 */
@Component
public class FootballEventAnalyzer {

    @Autowired
    private EventStatisticManager eventStatisticManager;

    public Optional<Boolean> isUnderPoints(List<Event<Team>> liveGameOdds, double points) {
        Event<Team> lastEventDetails = null;
        for (int i = liveGameOdds.size() - 1; i >= 0; i--) {
            if (liveGameOdds.get(i).getStatus() != EventStatus.CLOSED) {
                lastEventDetails = liveGameOdds.get(i);
                break;
            }
        }

        if (lastEventDetails == null) {
            return Optional.empty();
        }

        final Event<Team> event = lastEventDetails;
        OptionalInt goalCountOptional = IntStream.range(0, 10)
                .filter(goalNumber -> isAnyOptionAvailable(event, goalNumber + 0.5))
                .findFirst();

        if (goalCountOptional.isPresent() && goalCountOptional.getAsInt() > points) {
            return Optional.of(false);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Integer> getGoalCount(List<Event<Team>> liveGameOdds) {
        Event<Team> lastEventDetails = liveGameOdds.get(liveGameOdds.size() - 1);
        if (lastEventDetails.getStartTime().until(lastEventDetails.getUpdateTime(), ChronoUnit.MINUTES) >= 103) {
            return getGoalCount(lastEventDetails);
        }

        return Optional.empty();
    }

    public Optional<Integer> getGoalCount(Event<Team> event) {
        final Query query = new Query();
        final Team homeTeam = TeamUtils.getHomeTeam(event.getParticipants());
        final Team awayTeam = TeamUtils.getAwayTeam(event.getParticipants());
        query.addCriteria(Criteria.where("homeTeam").is(homeTeam));
        query.addCriteria(Criteria.where("awayTeam").is(awayTeam));
        Criteria startTimeCriteria = Criteria.where("startTime");
        LocalDateTime startOfDay = event.getStartTime().minusDays(1).withHour(0).withMinute(0);
        LocalDateTime endOfDay = event.getStartTime().plusDays(1).withHour(23).withMinute(59);
        startTimeCriteria.gte(startOfDay);
        startTimeCriteria.lte(endOfDay);
        query.addCriteria(startTimeCriteria);
        query.fields().exclude("goalStatistics");
        final List<EventStatistic> eventStatistics = eventStatisticManager.find(query);
        if (eventStatistics.size() == 1) {
            return Optional.of(eventStatistics.get(0).getGoalCount());
        }

        return EventUtils.estimateGoalCount(event);
    }

    /**
     * Return scores changes along with goal minute.
     */
    public Map<Score<Integer>, Event> getGoals(List<Event<Team>> liveGameOdds) {
        Map<Score<Integer>, Event> goalChanges = new LinkedHashMap<>();
        int lastGoalCount = 0;
        goalChanges.put(new Score<>(0, 0), liveGameOdds.get(0));
        for (Event<Team> event : liveGameOdds) {
            Optional<Score<Integer>> currentScore = getCurrentScore(event);
            if(!currentScore.isPresent()) {
                continue;
            }

            if(currentScore.get().getHomeSideScore() + currentScore.get().getAwaySideScore() > lastGoalCount) {
                long currentMinute = EventUtils.getCurrentMinute(event);
                goalChanges.put(currentScore.get(), event);
                lastGoalCount = currentScore.get().getHomeSideScore() + currentScore.get().getAwaySideScore();
            }
        }

        return goalChanges;
    }

    public static Optional<Score<Integer>> getCurrentScore(Event<Team> event) {
        for (LayBackBetLine layBackBetLine : event.getLayBackBetLines()) {
            if (layBackBetLine.getBetType() != BetType.CORRECT_SCORE ||
                    layBackBetLine.getPeriod() != Period.MATCH) {
                continue;
            }

            CorrectScoreLayBackBetLine correctScoreBetLine = (CorrectScoreLayBackBetLine) layBackBetLine;

            //should be available, and less than 100
            Map<Score<Integer>, BigDecimal> scoreToPriceMap = new LinkedHashMap<>();
            for (ScoreLayBackPrice scoreLayBackPrice : correctScoreBetLine.getScoreLayBackPriceList()) {
                Score<Integer> score = scoreLayBackPrice.getScoreSelection().getScore();
                //filter if lay is not available
                if (scoreLayBackPrice.getLayPrices().isEmpty()) {
                    continue;
                }

                BigDecimal price = null;
                if (CollectionUtils.isNotEmpty(scoreLayBackPrice.getBackPrices())) {
                    BigDecimal backPrice = ((BetPrice) scoreLayBackPrice.getBackPrices().get(0)).getPrice();
                    if (backPrice.doubleValue() < 100 && backPrice.doubleValue() > 0.001) {
                        price = backPrice;
                    }
                }
                if (price == null && CollectionUtils.isNotEmpty(scoreLayBackPrice.getLayPrices())) {
                    BigDecimal layPrice = ((BetPrice) scoreLayBackPrice.getLayPrices().get(0)).getPrice();
                    if (layPrice.doubleValue() < 100 && layPrice.doubleValue() > 0.001) {
                        price = layPrice;
                    }
                }

                if (price != null) {
                    scoreToPriceMap.put(score, price);
                }
            }

            if (scoreToPriceMap.isEmpty()) {
                return Optional.empty();
            }

            //now we should figure out current score
            scoreToPriceMap = scoreToPriceMap.entrySet()
                    .stream()
                    .sorted((score1, score2) -> {
                        int gameScore1 = score1.getKey().getHomeSideScore() + score1.getKey().getAwaySideScore();
                        int gameScore2 = score2.getKey().getHomeSideScore() + score2.getKey().getAwaySideScore();
                        return ((Integer) gameScore1).compareTo(gameScore2);
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));

            Iterator<Map.Entry<Score<Integer>, BigDecimal>> iterator = scoreToPriceMap.entrySet().iterator();
            Map.Entry<Score<Integer>, BigDecimal> firstScore = iterator.next();
            if (iterator.hasNext()) {
                Map.Entry<Score<Integer>, BigDecimal> secondScore = iterator.next();
                //if goal count is equal for both, than we don't know exact current goal score
                if (firstScore.getKey().getHomeSideScore() + firstScore.getKey().getAwaySideScore()
                        == secondScore.getKey().getHomeSideScore() + secondScore.getKey().getAwaySideScore()) {
                    //we don't know
                    return Optional.empty();
                }
            }

            return Optional.of(firstScore.getKey());
        }

        return Optional.empty();
    }
}
