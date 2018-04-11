package com.el.robot.analyzer.analytic.predictors;

import com.el.betting.sdk.v2.Score;
import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v3.statistic.EventStatistic;
import com.el.robot.analyzer.analytic.manager.EventStatisticProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 1 ) Take the game for example from German Bundesliga Freiburg - Borussia Dortmund to the test
 * 2) You go to any site where you can see the statistics of performances of these teams in the last five rounds.
 * 3 ) Then you must count how many goals scored Freiburg in the last 5 games. Goals scored at home count as 1.5 points . Goals scored away , count as 2 points.
 * <p>
 * Freiburg scored at home : 4 goal . 1.5 x 4 = 6 points
 * Freiburg scored Away : 3 goals . 2 x 3 = 6 points.
 * Total: 12 points Freiburg received in the last 5 games.
 * <p>
 * 4) consider how many goals Freiburg missed the last 5 games. Goals against home are counted as 1.5 points, a guest - 1 point.
 * <p>
 * Freiburg missed home : 2 goals. 1.5 x 2 = 3 points
 * Freiburg missed Away : 7 goals . 1 x 7 = 7 points.
 * Total: 10 points Freiburg lost in the last 5 matches.
 * <p>
 * 5) Calculate the average value of points scored and lost by Freiburg in the last 5 games. Amount of each indicator is divided by the number of matches , in this case 5 .
 * <p>
 * Scored: 12/5 = 2.4 points per game
 * Lost: 10/5 = 2 points per match
 * <p>
 * Do the same with the second team . In our case it Dortmund .
 * Averages in : Borussia Dortmund
 * <p>
 * Scored: 12.5 / 5 = 2.5 points per game
 * Lost: 16/5 = 3.2 points per game
 * <p>
 * 6) We have already received average values ​​gained and lost points both teams , it remains only to calculate the average values ​​in general.
 * <p>
 * 1. Points scored by Freiburg + Points lost by Borussia / 2
 * (2.4 + 3.2) / 2 = 2.8
 * <p>
 * 2 . Points lost by Freiburg + Points scored by Borussia / 2
 * (2 + 2.5) / 2 = 2.25
 * <p>
 * 7) Now we add both values ​​and divide by 2. This value is 2.5 “Over” indicator:
 * “Over” indicator 2.5 = (2.8 + 2.25) / 2 = 2,525
 * <p>
 * VERDICT:
 * If the indicator value is more than 2.5 - it will be over 2.5 at about 90%. While at first doubt something for reinsurance you can bet on Over 2.
 * NOTE (this is important)
 */
@Service
public class RateBaseGoalCountPredictor {

    private final static double homeScoreCoefficient = 2.5;
    private final static double awayScoreCoefficient = 1.5;

    /**
     * The importance between team scoring and conceding
     */
    private final static double scoreConcedeCoefficient = 2;
    private final static double homeConcedeCoefficient = 1;
    private final static double awayConcedeCoefficient = 0.6;

    @Autowired
    private EventStatisticProvider eventStatisticProvider;

    public Optional<Double> getExpectedGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit) {
        return getExpectedGoalCount(category, league, homeTeam, awayTeam, limit, null);
    }

    public Optional<Double> getExpectedGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, TimeRange<LocalDateTime> statsRange) {
        return getExpectedGoalCount(category, league, homeTeam, awayTeam, limit, null, statsRange);
    }

    public Optional<Double> getExpectedGoalCount(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, TimeRange<Integer> goalPeriod, TimeRange<LocalDateTime> statsRange) {
        final Optional<Score<Double>> scoreOptional = getExpectedScore(category, league, homeTeam, awayTeam, limit, goalPeriod, statsRange);
        if(scoreOptional.isPresent()) {
            final Score<Double> score = scoreOptional.get();
            return Optional.of(score.getHomeSideScore() + score.getAwaySideScore());
        }
        return Optional.empty();
    }

    public Optional<Score<Double>> getExpectedScore(@Nullable String category, @Nullable String league, String homeTeam, String awayTeam, int limit, TimeRange<Integer> goalPeriod, TimeRange<LocalDateTime> statsRange) {
        List<EventStatistic> homeTeamHomeGames = new ArrayList<>();
        List<EventStatistic> homeTeamAwayGames = new ArrayList<>();
        List<EventStatistic> awayTeamHomeGames = new ArrayList<>();
        List<EventStatistic> awayTeamAwayGames = new ArrayList<>();

        if(limit < 0) {
            List<EventStatistic> eventStatistics = eventStatisticProvider.getStatistics(category, league, homeTeam, awayTeam, true, true, true, statsRange);
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
            boolean excludeGoalStatistics = true;
            homeTeamHomeGames = eventStatisticProvider.getTeamStatistics(category, league, homeTeam, Team.Side.HOME, true, limit, excludeGoalStatistics, statsRange);
            awayTeamHomeGames = eventStatisticProvider.getTeamStatistics(category, league, awayTeam, Team.Side.HOME, true, limit, excludeGoalStatistics, statsRange);
            awayTeamAwayGames = eventStatisticProvider.getTeamStatistics(category, league, awayTeam, Team.Side.AWAY, true, limit, excludeGoalStatistics, statsRange);
            homeTeamAwayGames = eventStatisticProvider.getTeamStatistics(category, league, homeTeam, Team.Side.AWAY, true, limit, excludeGoalStatistics, statsRange);
        }

        //check that there are enough statistics for both teams
        if (homeTeamHomeGames.size() < limit || homeTeamAwayGames.size() < limit ||
                awayTeamHomeGames.size() < limit || awayTeamAwayGames.size() < limit) {
            return Optional.empty();
        }

        //calculate received and lost points
        double homeTeamScoreGoals = calculatePoints(Team.Side.HOME, homeTeamHomeGames, homeTeamAwayGames, goalPeriod) / ((homeTeamHomeGames.size() + homeTeamAwayGames.size()) / 2);
        double homeTeamConcedeGoals = calculatePoints(Team.Side.AWAY, homeTeamAwayGames, homeTeamHomeGames, goalPeriod) / ((homeTeamHomeGames.size() + homeTeamAwayGames.size()) / 2);

        double awayTeamScoreGoals = calculatePoints(Team.Side.AWAY, awayTeamHomeGames, awayTeamAwayGames, goalPeriod) / ((awayTeamHomeGames.size() + awayTeamAwayGames.size()) / 2);
        double awayTeamConcedeGoals = calculatePoints(Team.Side.HOME, awayTeamAwayGames, awayTeamHomeGames, goalPeriod) / ((awayTeamHomeGames.size() + awayTeamAwayGames.size()) / 2);

        double expectedHomeTeamGoals = (scoreConcedeCoefficient * homeTeamScoreGoals + awayTeamConcedeGoals) / (scoreConcedeCoefficient + 1);
        double expectedAwayTeamGoals = (scoreConcedeCoefficient * awayTeamScoreGoals + homeTeamConcedeGoals) / (scoreConcedeCoefficient + 1);

        return Optional.of(new Score<>(expectedHomeTeamGoals, expectedAwayTeamGoals));
    }

    private double calculatePoints(Team.Side side, List<EventStatistic> homeGames, List<EventStatistic> awayGames, TimeRange<Integer> goalPeriod) {
        final ToIntFunction<EventStatistic> homeGoalCountFunction = AverageGoalCountPredictor.getGoalCountFunction(goalPeriod, Team.Side.HOME);
        final ToIntFunction<EventStatistic> awayGoalCountFunction = AverageGoalCountPredictor.getGoalCountFunction(goalPeriod, Team.Side.AWAY);
        double receivedPoints = homeGames.stream()
                .collect(Collectors.summingInt(homeGoalCountFunction))
                * (side == Team.Side.HOME ? homeScoreCoefficient : awayScoreCoefficient)
                +
                awayGames.stream()
                        .collect(Collectors.summingInt(awayGoalCountFunction))
                        * (side == Team.Side.AWAY ? homeScoreCoefficient : awayScoreCoefficient);

        return receivedPoints / (homeScoreCoefficient + awayScoreCoefficient);
    }

    private double calculateLostPoints(Team.Side side, List<EventStatistic> homeGames, List<EventStatistic> awayGames) {
        double lostPoints = homeGames.stream()
                .collect(Collectors.summingInt(EventStatistic::getAwayTeamGoalCount))
                * (side == Team.Side.HOME ? homeConcedeCoefficient : awayConcedeCoefficient)
                +
                awayGames.stream()
                        .collect(Collectors.summingInt(EventStatistic::getHomeTeamGoalCount)) * awayConcedeCoefficient;

        return lostPoints / (homeConcedeCoefficient + awayConcedeCoefficient);
    }
}
