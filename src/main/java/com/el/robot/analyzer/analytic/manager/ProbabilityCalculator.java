package com.el.robot.analyzer.analytic.manager;

import com.el.betting.common.CollectionUtil;
import com.el.betting.common.Predicates;
import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;
import com.el.betting.sdk.v3.statistic.TeamDescriptiveStats;
import com.el.robot.crawler.db.v3.TeamDescriptiveStatsManager;
import org.apache.commons.math3.stat.StatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Function;

@Service
public class ProbabilityCalculator {

    @Autowired
    private TeamDescriptiveStatsManager teamDescriptiveStatsManager;

    public Optional<Double> getUnderProbability(String category, String league, String homeTeam, String awayTeam, Function<GoalsDescriptiveStats, Double> underMethod) {
        final TeamDescriptiveStats homeTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, homeTeam);
        final TeamDescriptiveStats awayTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, awayTeam);
        if (homeTeamDescriptiveStats == null || awayTeamDescriptiveStats == null) {
            return Optional.empty();
        }

        GoalsDescriptiveStats homeScoredStats = homeTeamDescriptiveStats.getHomeScoredStats();
        GoalsDescriptiveStats homeConcededStats = homeTeamDescriptiveStats.getHomeConcededStats();
        GoalsDescriptiveStats awayScoredStats = awayTeamDescriptiveStats.getAwayScoredStats();
        GoalsDescriptiveStats awayConcededStats = awayTeamDescriptiveStats.getAwayConcededStats();

        return getUnderProbability(homeScoredStats, homeConcededStats, awayScoredStats, awayConcededStats, underMethod);
    }

    public Optional<Double> getUnderCumulativeProbability(String category, String league, String homeTeam, String awayTeam, int goalCount) {
        final TeamDescriptiveStats homeTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, homeTeam);
        final TeamDescriptiveStats awayTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, awayTeam);
        if (homeTeamDescriptiveStats == null || awayTeamDescriptiveStats == null) {
            return Optional.empty();
        }

        GoalsDescriptiveStats homeScoredStats = homeTeamDescriptiveStats.getHomeScoredStats();
        GoalsDescriptiveStats homeConcededStats = homeTeamDescriptiveStats.getHomeConcededStats();
        GoalsDescriptiveStats awayScoredStats = awayTeamDescriptiveStats.getAwayScoredStats();
        GoalsDescriptiveStats awayConcededStats = awayTeamDescriptiveStats.getAwayConcededStats();

        return getUnderCumulativeProbability(homeScoredStats, homeConcededStats, awayScoredStats, awayConcededStats, goalCount);
    }

    /**
     * Calculates probability of how many times team will score less than given goal count.
     */
    public Optional<Double> getUnderProbability(GoalsDescriptiveStats homeScoredStats, GoalsDescriptiveStats awayConcededStats, Function<GoalsDescriptiveStats, Double> underMethod) {
        if (homeScoredStats == null && awayConcededStats == null) {
            return Optional.empty();
        }

        final Optional<Double> homeScoreProbability = CollectionUtil.applyIfValid(Predicates.notNull(), underMethod, homeScoredStats, awayConcededStats)
                .stream()
                .reduce((a, b) -> StatUtils.geometricMean(new double[]{a, b})
                );

        return homeScoreProbability;
    }

    /**
     * Calculates probability of how many time teams will score less than given points, separately.
     * E.G. How many time both teams separately will score less than 2.5 goals
     */
    public Optional<Double> getUnderProbability(GoalsDescriptiveStats homeScoredStats, GoalsDescriptiveStats homeConcededStats, GoalsDescriptiveStats awayScoredStats, GoalsDescriptiveStats awayConcededStats, Function<GoalsDescriptiveStats, Double> underMethod) {
        if (homeScoredStats == null && awayConcededStats == null
                || awayScoredStats == null && homeConcededStats == null) {
            return Optional.empty();
        }

        final Optional<Double> homeScoreProbability = CollectionUtil.applyIfValid(Predicates.notNull(), underMethod, homeScoredStats, awayConcededStats)
                .stream()
                .reduce(
                        (a, b) -> StatUtils.geometricMean(new double[]{a, b})
                );

        final Optional<Double> homeConcedeProbability = CollectionUtil.applyIfValid(Predicates.notNull(), underMethod, awayScoredStats, homeConcededStats)
                .stream()
                .reduce(
                        (a, b) -> StatUtils.geometricMean(new double[]{a, b})
                );

        return Optional.of(StatUtils.geometricMean(new double[]{homeScoreProbability.get(), homeConcedeProbability.get()}));
    }

    /**
     * Calculate probability of both teams together scoring less or equal than given points
     * if points = 1, it means the final result could be 0-0, 1-0, 0-1
     */
    public Optional<Double> getUnderCumulativeProbability(GoalsDescriptiveStats homeScoredStats, GoalsDescriptiveStats homeConcededStats, GoalsDescriptiveStats awayScoredStats, GoalsDescriptiveStats awayConcededStats, int goalCount) {
        if (homeScoredStats == null && awayConcededStats == null
                || awayScoredStats == null && homeConcededStats == null) {
            return Optional.empty();
        }

        //e.g. less or equal than 4 goals
        //there are those combinations
        //team 1 scores under 4.5, team2 under 0.5
        //team 1 scores under 3.5, team2 under 1.5
        //team 1 scores under 2.5, team2 under 2.5
        //team 1 scores under 1.5, team2 under 3.5
        //team 1 scores under 0.5, team2 under 4.5
        double probability = 0;
        for (double homeScoreGoals = goalCount + 0.5; homeScoreGoals >= 0.5; homeScoreGoals--) {
            final double awayScoreGoals = goalCount + 1 - homeScoreGoals;
            final Function<GoalsDescriptiveStats, Double> homeScoreUnderMethod = getUnderMethod(homeScoreGoals);
            final Function<GoalsDescriptiveStats, Double> awayScoreUnderMethod = getUnderMethod(awayScoreGoals);
            final Optional<Double> homeScoreUnderProbability = getUnderProbability(homeScoredStats, awayConcededStats, homeScoreUnderMethod);
            final Optional<Double> awayScoreUnderProbability = getUnderProbability(homeConcededStats, awayScoredStats, awayScoreUnderMethod);
            probability += homeScoreUnderProbability.get() * awayScoreUnderProbability.get();
        }

        return Optional.of(Math.min(probability, 1));
    }

    private Function<GoalsDescriptiveStats, Double> getUnderMethod(double points) {
        int intValue = (int) (points - 0.5);
        switch (intValue) {
            case 0:
                return GoalsDescriptiveStats::getProbabilityUnder05;
            case 1:
                return GoalsDescriptiveStats::getProbabilityUnder15;
            case 2:
                return GoalsDescriptiveStats::getProbabilityUnder25;
            case 3:
                return GoalsDescriptiveStats::getProbabilityUnder35;
            case 4:
                return GoalsDescriptiveStats::getProbabilityUnder45;
            case 5:
                return GoalsDescriptiveStats::getProbabilityUnder55;
            default:
                throw new RuntimeException("Can't find goal count probability for " + intValue);
        }
    }


    public Optional<Double> getCorrectScoreProbability(String category, String league, String homeTeam, String awayTeam, int homeGoalScored, int awayGoalScored) {
        final TeamDescriptiveStats homeTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, homeTeam);
        final TeamDescriptiveStats awayTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, awayTeam);
        if (homeTeamDescriptiveStats == null || awayTeamDescriptiveStats == null) {
            return Optional.empty();
        }

        GoalsDescriptiveStats homeScoredStats = homeTeamDescriptiveStats.getHomeScoredStats();
        GoalsDescriptiveStats homeConcededStats = homeTeamDescriptiveStats.getHomeConcededStats();
        GoalsDescriptiveStats awayScoredStats = awayTeamDescriptiveStats.getAwayScoredStats();
        GoalsDescriptiveStats awayConcededStats = awayTeamDescriptiveStats.getAwayConcededStats();

        return getCorrectScoreProbability(homeScoredStats, homeConcededStats, awayScoredStats, awayConcededStats, homeGoalScored, awayGoalScored);
    }

    public Optional<Double> getCorrectScoreProbability(GoalsDescriptiveStats homeScoredStats, GoalsDescriptiveStats homeConcededStats, GoalsDescriptiveStats awayScoredStats, GoalsDescriptiveStats awayConcededStats, int homeGoalCount, int awayGoalCount) {
        if (homeScoredStats == null && awayConcededStats == null
                || awayScoredStats == null && homeConcededStats == null) {
            return Optional.empty();
        }

        //e.g. 2 - 1
        //it means probability that home team will score less than 2.5 and more than 1.5
        final Optional<Double> homeScoreProbability = getCorrectScoreProbability(homeScoredStats, awayConcededStats, homeGoalCount);
        final Optional<Double> homeConcedeProbability = getCorrectScoreProbability(homeConcededStats, awayScoredStats, awayGoalCount);
        return Optional.of(homeScoreProbability.get() * homeConcedeProbability.get());
    }

    /**
     * Returns team goal count probability
     */
    public Optional<Double> getCorrectScoreProbability(GoalsDescriptiveStats homeScoredStats, GoalsDescriptiveStats awayConcededStats, int goalCount) {
        //e.g. probability of goal scored - 2
        //it means probability that home team will score less than 2.5 and more than 1.5
        Double homeScore = null;
        if(homeScoredStats != null) {
            homeScore = homeScoredStats.getGoalFrequencies().getOrDefault(goalCount, 0.1d);
        }

        Double awayConcede = null;
        if(awayConcededStats != null) {
            awayConcede = awayConcededStats.getGoalFrequencies().getOrDefault(goalCount, 0.1d);
        }

        if(homeScore != null && awayConcede != null) {
            return Optional.of(StatUtils.geometricMean(new double[]{homeScore, awayConcede}));
        } else {
            return Optional.ofNullable(CollectionUtil.getFirstNotNull(homeScore, awayConcede));
        }
    }
}
