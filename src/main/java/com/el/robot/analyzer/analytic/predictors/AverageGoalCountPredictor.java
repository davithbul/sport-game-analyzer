package com.el.robot.analyzer.analytic.predictors;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v2.common.TimeRanges;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * Calculates average goal count scored by both teams. It uses simple mathematical
 * avg calculation.
 */
@Component
public class AverageGoalCountPredictor {

    @Autowired
    private EventStatisticProvider eventStatisticProvider;

    public Optional<Double> getAvgGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, TimeRange<LocalDateTime> statsRange) {
        return getAvgGoalCount(category, league, homeTeam, awayTeam, statsRange, null);
    }


    public Optional<Double> getAvgGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, @Nullable TimeRange<LocalDateTime> statsRange, @Nullable TimeRange<Integer> goalPeriod) {
        return getAvgGoalCount(category, league, homeTeam, awayTeam, true, -1, goalPeriod, statsRange);
    }

    public Optional<Double> getAvgGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit) {
        return getAvgGoalCount(category, league, homeTeam, awayTeam, true, limit, null, null);
    }

    public Optional<Double> getAvgGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, boolean ignoreSides, int limit, TimeRange<Integer> goalPeriod, TimeRange<LocalDateTime> statsRange) {
        final Optional<Score<Double>> avgScore = getAvgScore(category, league, homeTeam, awayTeam, ignoreSides, limit, goalPeriod, statsRange);
        if(avgScore.isPresent()) {
            return Optional.of(avgScore.get().getHomeSideScore() + avgScore.get().getAwaySideScore());
        }
        return Optional.empty();
    }


    /**
     * Predicts goal scored based on average goal scored by teams in the given date range (or overall if not specified).
     * The predictor doesn't take into account home or away ground when calculating average.
     *
     * @param homeTeam           the name of home team
     * @param awayTeam           the name of away team
     * @param statsRange         the time period for which the average will be calculated
     * @return average goal count, or empty if there are no enough statistical data
     */
    public Optional<Score<Double>> getAvgScore(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, boolean ignoreSide, int limit, TimeRange<Integer> goalPeriod, TimeRange<LocalDateTime> statsRange) {
        List<EventStatistic> homeTeamHomeGames = new ArrayList<>();
        List<EventStatistic> homeTeamAwayGames = new ArrayList<>();
        List<EventStatistic> awayTeamHomeGames = new ArrayList<>();
        List<EventStatistic> awayTeamAwayGames = new ArrayList<>();

        final boolean excludeGoalsStats = (goalPeriod == null);
        if (limit < 0) {
            List<EventStatistic> eventStatistics = eventStatisticProvider.getStatistics(category, league, homeTeam, awayTeam, ignoreSide, true, excludeGoalsStats, statsRange);
            for (EventStatistic eventStatistic : eventStatistics) {
                if (eventStatistic.getHomeTeam().equals(homeTeam)) {
                    homeTeamHomeGames.add(eventStatistic);
                } else if (eventStatistic.getHomeTeam().equals(awayTeam)) {
                    awayTeamHomeGames.add(eventStatistic);
                } else if (eventStatistic.getAwayTeam().equals(homeTeam)) {
                    homeTeamAwayGames.add(eventStatistic);
                } else if (eventStatistic.getAwayTeam().equals(awayTeam)) {
                    awayTeamAwayGames.add(eventStatistic);
                }
            }
        } else {
            homeTeamHomeGames = eventStatisticProvider.getTeamStatistics(category, league, homeTeam, Team.Side.HOME, true, limit, excludeGoalsStats, statsRange);
            awayTeamHomeGames = eventStatisticProvider.getTeamStatistics(category, league, awayTeam, Team.Side.HOME, true, limit, excludeGoalsStats, statsRange);
            awayTeamAwayGames = eventStatisticProvider.getTeamStatistics(category, league, awayTeam, Team.Side.AWAY, true, limit, excludeGoalsStats, statsRange);
            homeTeamAwayGames = eventStatisticProvider.getTeamStatistics(category, league, homeTeam, Team.Side.AWAY, true, limit, excludeGoalsStats, statsRange);
        }

        //check that there are enough statistics for both teams
        if (homeTeamHomeGames.size() < limit || homeTeamAwayGames.size() < limit ||
                awayTeamHomeGames.size() < limit || awayTeamAwayGames.size() < limit) {
            return Optional.empty();
        }

        //calculate average
        final double homeAvgGoalCount = getAvgGoalCount(getGoalCountFunction(goalPeriod), homeTeamHomeGames, homeTeamAwayGames);
        final double awayAvgGoalCount = getAvgGoalCount(getGoalCountFunction(goalPeriod), awayTeamAwayGames, awayTeamHomeGames);
        return Optional.of(new Score<>(null, homeAvgGoalCount / 2, awayAvgGoalCount / 2));
    }


    @SafeVarargs
    protected static double getAvgGoalCount(ToIntFunction<EventStatistic> mapper, List<EventStatistic>... eventStatistics) {
        return Arrays.stream(eventStatistics)
                .mapToDouble(eventStatistic -> getAvgGoalCount(eventStatistic, mapper))
                .sum()
                / eventStatistics.length;
    }

    protected static double getAvgGoalCount(List<EventStatistic> eventStatistics, ToIntFunction<EventStatistic> mapper) {
        return (double) eventStatistics.stream().collect(Collectors.summingInt(mapper)) / eventStatistics.size();
    }

    protected static ToIntFunction<EventStatistic> getGoalCountFunction(@Nullable TimeRange<Integer> timePeriod) {
        if (timePeriod == null) {
            return EventStatistic::getGoalCount;
        }

        return (eventStatistic -> (int) eventStatistic.getGoalStatistics().stream()
                .filter(goalStatistic -> TimeRanges.isWithin(goalStatistic.getGoalMinute(), timePeriod))
                .count());
    }

    protected static ToIntFunction<EventStatistic> getGoalCountFunction(@Nullable TimeRange<Integer> timePeriod, Team.Side side) {
        Objects.requireNonNull(side);

        if (timePeriod == null) {
            switch (side) {
                case HOME:
                    return EventStatistic::getHomeTeamGoalCount;
                case AWAY:
                    return EventStatistic::getAwayTeamGoalCount;
                default:
                    return EventStatistic::getGoalCount;
            }
        }

        return (eventStatistic -> (int) eventStatistic.getGoalStatistics().stream()
                .filter(goalStatistic ->
                        TimeRanges.isWithin(goalStatistic.getGoalMinute(), timePeriod)
                                && (
                                side == Team.Side.HOME ?
                                        goalStatistic.getTeamName().equals(eventStatistic.getHomeTeam()) :
                                        goalStatistic.getTeamName().equals(eventStatistic.getAwayTeam())
                        ))
                .count());
    }
}
