package com.el.robot.analyzer.analytic.predictors;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.el.robot.analyzer.analytic.predictors.AverageGoalCountPredictor.getAvgGoalCount;
import static com.el.robot.crawler.db.util.EventStatisticUtils.getLeagueName;

@Component
public class StrengthBaseGoalCountPredictor {

    @Autowired
    private EventStatisticProvider eventStatisticProvider;

    public Optional<Double> getExpectedGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int gameCount) {
        return getExpectedGoalCount(category, league, homeTeam, awayTeam, gameCount, null);
    }

    /**
     * Uses http://www.pinnaclesports.com/en/betting-articles/soccer/how-to-calculate-poisson-distribution formulta
     * for calculation average goal count.
     */
    public Optional<Double> getExpectedGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int gameCount, TimeRange<LocalDateTime> statsRange) {
        final Optional<Score<Double>> expectedScore = getExpectedScore(category, league, homeTeam, awayTeam, gameCount, statsRange);
        if(expectedScore.isPresent()) {
            final Score<Double> score = expectedScore.get();
            return Optional.of(score.getHomeSideScore() + score.getAwaySideScore());
        }
        return Optional.empty();
    }

    /**
     * Uses http://www.pinnaclesports.com/en/betting-articles/soccer/how-to-calculate-poisson-distribution formulta
     * for calculation average goal count.
     */
    public Optional<Score<Double>> getExpectedScore(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int gameCount, TimeRange<LocalDateTime> statsRange) {
        int eachSideGameCount = gameCount %2 == 0 ? gameCount / 2 : gameCount / 2 + 1;
        List<EventStatistic> homeTeamHomeStats = eventStatisticProvider.getTeamStatistics(category, league, homeTeam, Team.Side.HOME, true, eachSideGameCount, true, statsRange);
        List<EventStatistic> awayTeamAwayStats = eventStatisticProvider.getTeamStatistics(category, league, awayTeam, Team.Side.AWAY, true, eachSideGameCount, true, statsRange);

        if (homeTeamHomeStats.size() + awayTeamAwayStats.size() < gameCount) {
            return Optional.empty();
        }

        double avgHomeTeamHomeScoredGoalCount = getAvgGoalCount(homeTeamHomeStats, EventStatistic::getHomeTeamGoalCount);
        double avgHomeTeamHomeConcededGoalCount = getAvgGoalCount(homeTeamHomeStats, EventStatistic::getAwayTeamGoalCount);
        double avgAwayTeamAwayScoredGoalCount = getAvgGoalCount(awayTeamAwayStats, EventStatistic::getAwayTeamGoalCount);
        double avgAwayTeamAwayConcedeGoalCount = getAvgGoalCount(awayTeamAwayStats, EventStatistic::getHomeTeamGoalCount);


        if (league == null) {
            league = getLeagueName(homeTeamHomeStats, awayTeamAwayStats);
            if (league == null) {
                return Optional.empty();
            }
        }

        List<EventStatistic> leagueStats = eventStatisticProvider.getLeagueStats(category, league, gameCount, statsRange);
        double avgLeagueHomeTeamGoalCount = (double) leagueStats.stream().collect(Collectors.summingInt(EventStatistic::getHomeTeamGoalCount)) / leagueStats.size();
        double avgLeagueAwayTeamGoalCount = (double) leagueStats.stream().collect(Collectors.summingInt(EventStatistic::getAwayTeamGoalCount)) / leagueStats.size();

        double homeTeamAttackRate = avgHomeTeamHomeScoredGoalCount / avgLeagueHomeTeamGoalCount;
        double awayTeamDefenceRate = avgAwayTeamAwayConcedeGoalCount / avgLeagueHomeTeamGoalCount;

        //Man United’s Goals = Man United’s Attack x Swansea’s Defence x Average No. Goals
        double expectedHomeTeamGoalCount = homeTeamAttackRate * awayTeamDefenceRate * avgLeagueHomeTeamGoalCount;


        double awayTeamAttackRate = avgAwayTeamAwayScoredGoalCount / avgLeagueAwayTeamGoalCount;
        double homeTeamDefenceRate = avgHomeTeamHomeConcededGoalCount / avgLeagueAwayTeamGoalCount;

        //Swansea’s Goals = Swansea’s Attack x Man United’s Defence x Average No. Goals
        double expectedAwayTeamGoalCount = awayTeamAttackRate * homeTeamDefenceRate * avgLeagueAwayTeamGoalCount;

        return Optional.of(new Score<>(expectedHomeTeamGoalCount, expectedAwayTeamGoalCount));
    }


    protected Optional<Score<Double>> getExpectedScore(List<EventStatistic> homeTeamHomeStats, List<EventStatistic> awayTeamAwayStats, List<EventStatistic> leagueStats) {
        double avgHomeTeamHomeScoredGoalCount = getAvgGoalCount(homeTeamHomeStats, EventStatistic::getHomeTeamGoalCount);
        double avgHomeTeamHomeConcededGoalCount = getAvgGoalCount(homeTeamHomeStats, EventStatistic::getAwayTeamGoalCount);
        double avgAwayTeamAwayScoredGoalCount = getAvgGoalCount(awayTeamAwayStats, EventStatistic::getAwayTeamGoalCount);
        double avgAwayTeamAwayConcedeGoalCount = getAvgGoalCount(awayTeamAwayStats, EventStatistic::getHomeTeamGoalCount);

        double avgLeagueHomeTeamGoalCount = (double) leagueStats.stream().collect(Collectors.summingInt(EventStatistic::getHomeTeamGoalCount)) / leagueStats.size();
        double avgLeagueAwayTeamGoalCount = (double) leagueStats.stream().collect(Collectors.summingInt(EventStatistic::getAwayTeamGoalCount)) / leagueStats.size();

        double homeTeamAttackRate = avgHomeTeamHomeScoredGoalCount / avgLeagueHomeTeamGoalCount;
        double awayTeamDefenceRate = avgAwayTeamAwayConcedeGoalCount / avgLeagueHomeTeamGoalCount;

        //Man United’s Goals = Man United’s Attack x Swansea’s Defence x Average No. Goals
        double expectedHomeTeamGoalCount = homeTeamAttackRate * awayTeamDefenceRate * avgLeagueHomeTeamGoalCount;


        double awayTeamAttackRate = avgAwayTeamAwayScoredGoalCount / avgLeagueAwayTeamGoalCount;
        double homeTeamDefenceRate = avgHomeTeamHomeConcededGoalCount / avgLeagueAwayTeamGoalCount;

        //Swansea’s Goals = Swansea’s Attack x Man United’s Defence x Average No. Goals
        double expectedAwayTeamGoalCount = awayTeamAttackRate * homeTeamDefenceRate * avgLeagueAwayTeamGoalCount;
        return Optional.of(new Score<>(expectedHomeTeamGoalCount, expectedAwayTeamGoalCount));
    }

    protected Optional<Double> getExpectedGoalCount(List<EventStatistic> homeTeamHomeStats, List<EventStatistic> awayTeamAwayStats, List<EventStatistic> leagueStats) {
        final Optional<Score<Double>> expectedScore = getExpectedScore(homeTeamHomeStats, awayTeamAwayStats, leagueStats);
        if(expectedScore.isPresent()) {
            final Score<Double> score = expectedScore.get();
            return Optional.of(score.getHomeSideScore() + score.getAwaySideScore());
        }
        return Optional.empty();
    }
}
