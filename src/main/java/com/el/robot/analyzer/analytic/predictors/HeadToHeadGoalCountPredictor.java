package com.el.robot.analyzer.analytic.predictors;

import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;

import static com.el.robot.analyzer.analytic.predictors.AverageGoalCountPredictor.getAvgGoalCount;
import static com.el.robot.analyzer.analytic.predictors.AverageGoalCountPredictor.getGoalCountFunction;
import static com.el.robot.crawler.db.util.EventStatisticUtils.getLeagueName;

/**
 * Returns home team against awayTeam statistics. It doesn't check the team statistics
 * against other teams.
 */
@Service
public class HeadToHeadGoalCountPredictor {

    @Autowired
    private EventStatisticProvider eventStatisticProvider;


    @Autowired
    private StrengthBaseGoalCountPredictor strengthBaseGoalCountPredictor;

    public Optional<Double> getExpectedGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit) {
        return getExpectedGoalCountStrengthBase(category, league, homeTeam, awayTeam, limit, null);
    }

    /**
     * @param limit indicates the game count which will be used for comparing, if -1 it means there is no limit.
     */
    public Optional<Double> getExpectedGoalCountStrengthBase(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, TimeRange<LocalDateTime> statsRange) {
        List<EventStatistic> headToHeadStats = getHeadToHeadStats(category, league, homeTeam, awayTeam, limit, true, statsRange);

        if (headToHeadStats.size() < limit) {
            return Optional.empty();
        }

        if (league == null) {
            league = getLeagueName(headToHeadStats);
        }

        List<EventStatistic> leagueStats = eventStatisticProvider.getLeagueStats(category, league, headToHeadStats.size(), statsRange);
        return strengthBaseGoalCountPredictor.getExpectedGoalCount(headToHeadStats, headToHeadStats, leagueStats);
    }

    public Optional<Double> getExpectedGoalCountIgnoreSide(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit) {
        return getExpectedGoalCountIgnoreSide(category, league, homeTeam, awayTeam, limit, null);
    }

    public Optional<Double> getExpectedGoalCountIgnoreSide(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, TimeRange<LocalDateTime> statsRange) {
        int eachSideGameCount = limit % 2 == 0 ? limit / 2 : limit / 2 + 1;
        List<EventStatistic> homeAwayStats = getHeadToHeadStats(category, league, homeTeam, awayTeam, eachSideGameCount, true, statsRange);
        List<EventStatistic> awayHomeStats = getHeadToHeadStats(category, league, awayTeam, homeTeam, eachSideGameCount, true, statsRange);

        if (homeAwayStats.size() + awayHomeStats.size() < limit) {
            return Optional.empty();
        }
        limit = homeAwayStats.size() + awayHomeStats.size();

        if (league == null) {
            league = getLeagueName(homeAwayStats, awayHomeStats);
            if (league == null) {
                return Optional.empty();
            }
        }

        List<EventStatistic> leagueStats = eventStatisticProvider.getLeagueStats(category, league, limit, statsRange);
        Optional<Double> expectedGoalCount = strengthBaseGoalCountPredictor.getExpectedGoalCount(homeAwayStats, homeAwayStats, leagueStats);
        Optional<Double> flippedExpectedGoalCount = strengthBaseGoalCountPredictor.getExpectedGoalCount(awayHomeStats, awayHomeStats, leagueStats);

        if (!expectedGoalCount.isPresent() || !flippedExpectedGoalCount.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(expectedGoalCount.get() + flippedExpectedGoalCount.get());
    }

    public Optional<Double> getAverageGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit) {
        return getAverageGoalCount(category, league, homeTeam, awayTeam, limit, null, null);
    }

    public Optional<Double> getAverageGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, TimeRange<Integer> goalTimePeriod, TimeRange<LocalDateTime> statsRange) {
        List<EventStatistic> headToHeadStats = getHeadToHeadStats(category, league, homeTeam, awayTeam, limit, goalTimePeriod == null, statsRange);
        if (headToHeadStats.size() < limit) {
            return Optional.empty();
        }
        final ToIntFunction<EventStatistic> goalCountFunction = getGoalCountFunction(goalTimePeriod);
        return Optional.of(getAvgGoalCount(headToHeadStats, goalCountFunction));
    }

    public Optional<Double> getAverageGoalCountIgnoreSide(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit) {
        return getAverageGoalCountIgnoreSide(category, league, homeTeam, awayTeam, limit, null, null);
    }

    public Optional<Double> getAverageGoalCountIgnoreSide(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, TimeRange<Integer> goalTimePeriod, TimeRange<LocalDateTime> statsRange) {
        int eachSideGameCount = limit % 2 == 0 ? limit / 2 : limit / 2 + 1;
        List<EventStatistic> homeAwayStats = getHeadToHeadStats(category, league, homeTeam, awayTeam, eachSideGameCount, goalTimePeriod == null, statsRange);
        List<EventStatistic> awayHomeStats = getHeadToHeadStats(category, league, awayTeam, homeTeam, eachSideGameCount, goalTimePeriod == null, statsRange);

        if (homeAwayStats.size() + awayHomeStats.size() < limit) {
            return Optional.empty();
        }
        limit = homeAwayStats.size() + awayHomeStats.size();

        if (league == null) {
            league = getLeagueName(homeAwayStats, awayHomeStats);
            if (league == null) {
                return Optional.empty();
            }
        }

        final ToIntFunction<EventStatistic> goalCountFunction = getGoalCountFunction(goalTimePeriod);
        final double avgGoalCount = (getAvgGoalCount(homeAwayStats, goalCountFunction)
                +
                getAvgGoalCount(awayHomeStats, goalCountFunction)) / 2;
        return Optional.of(avgGoalCount);
    }

    private List<EventStatistic> getHeadToHeadStats(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, boolean excludeGoalStatistics, TimeRange<LocalDateTime> statsRange) {
        return eventStatisticProvider.getStatistics(category, league, homeTeam, awayTeam, false, limit > 0, limit, excludeGoalStatistics, statsRange);
    }
}
