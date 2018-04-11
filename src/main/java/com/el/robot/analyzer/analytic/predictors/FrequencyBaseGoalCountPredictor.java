package com.el.robot.analyzer.analytic.predictors;

import com.el.betting.common.OptionalElements;
import com.el.betting.sdk.v2.Team.Side;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.betting.sdk.v3.statistic.GoalStatistic;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

import static com.el.robot.analyzer.analytic.predictors.AverageGoalCountPredictor.getGoalCountFunction;

/**
 * Returns statistics about how many times team scored more / less than
 * given goal count.
 */
@Component
public class FrequencyBaseGoalCountPredictor {

    @Autowired
    private EventStatisticProvider eventStatisticProvider;

    public Optional<Double> getOverGoalCountFrequency(double points, @Nullable Side side, @Nullable String category, @Nullable String league, String team, int gameCount) {
        return getOverGoalCountFrequency(points, side, category, league, team, gameCount, null, null);
    }

    /**
     * Returns the percent between 0 - 1, how many times team scored more than given points
     */
    public Optional<Double> getOverGoalCountFrequency(double points, @Nullable Side side, @Nullable String category, @Nullable String league, String team, int gameCount, TimeRange<Integer> goalPeriod, TimeRange<LocalDateTime> statsTimeRange) {
        List<EventStatistic> teamStats = eventStatisticProvider.getTeamStatistics(category, league, team, side, true, gameCount, goalPeriod == null, statsTimeRange);
        if (teamStats.size() < gameCount) {
            return Optional.empty();
        }

        final ToIntFunction<EventStatistic> goalCountFunction = getGoalCountFunction(goalPeriod);
        long overGoalGameCount = teamStats.stream()
                .filter(eventStatistic -> goalCountFunction.applyAsInt(eventStatistic) > points)
                .count();


        return Optional.of((double) overGoalGameCount / teamStats.size());
    }

    public Optional<Double> getAvgOverGoalCountFrequency(double points, @Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int gameCount, boolean ignoreSide) {
        return getAvgOverGoalCountFrequency(points, category, league, homeTeam, awayTeam, gameCount, ignoreSide, null, null);
    }

    /**
     * Return the percent, how many times an average both teams scored more than given goal count.
     */
    public Optional<Double> getAvgOverGoalCountFrequency(double points, @Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int gameCount, boolean ignoreSide, @Nullable TimeRange<Integer> goalPeriod, TimeRange<LocalDateTime> statsTimeRange) {
        Optional<Double> homeTeamOverGoalCountFrequency = getOverGoalCountFrequency(points, ignoreSide ? null : Side.HOME, category, league, homeTeam, gameCount, goalPeriod, statsTimeRange);
        Optional<Double> awayTeamOverGoalCountFrequency = getOverGoalCountFrequency(points, ignoreSide ? null : Side.AWAY, category, league, awayTeam, gameCount, goalPeriod, statsTimeRange);
        return OptionalElements.allPresent(homeTeamOverGoalCountFrequency, awayTeamOverGoalCountFrequency) ?
                Optional.of((homeTeamOverGoalCountFrequency.get() + awayTeamOverGoalCountFrequency.get()) / 2) :
                Optional.empty();
    }

    /**
     * Returns the percent of the cases, where given team scored goal during
     * given time period.
     */
    public Optional<Double> getGoalFrequencyForPeriod(@Nullable String category, @Nullable String league, String teamName, int limit, TimeRange<Integer> goalPeriod, TimeRange<LocalDateTime> statsTimeRange) {
        List<EventStatistic> eventStatistics = eventStatisticProvider.getTeamStatistics(category, league, teamName, null, true, limit, false, statsTimeRange);

        if (eventStatistics.size() < limit) {
            return Optional.empty();
        }

        int goalScoredOnThePeriod = 0;
        for (EventStatistic eventStatistic : eventStatistics) {
            for (GoalStatistic goalStatistic : eventStatistic.getGoalStatistics()) {
                if (goalStatistic.getTeamName().equals(teamName)
                        && goalStatistic.getGoalMinute() >= goalPeriod.getFrom() - 1
                        && goalStatistic.getGoalMinute() <= goalPeriod.getTo() + 1) {
                    goalScoredOnThePeriod++;
                    break;
                }
            }
        }

        return Optional.of((double) (goalScoredOnThePeriod * 100) / eventStatistics.size());
    }
}
