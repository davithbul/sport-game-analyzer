package com.el.robot.analyzer.analytic.manager;

import com.el.betting.common.CollectionUtil;
import com.el.betting.common.Predicates;
import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;
import com.el.betting.sdk.v3.statistic.TeamDescriptiveStats;
import com.el.robot.crawler.db.v3.TeamDescriptiveStatsManager;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;

public class StdDevBaseStabilityMeasure extends TeamStabilityMeasure {

    private TeamDescriptiveStatsManager teamDescriptiveStatsManager;

    public Optional<Double> maxGoalCount(String category, String league, String homeTeam, String awayTeam, boolean strict, Function<GoalsDescriptiveStats, Double> getPercentile) {
        final TeamDescriptiveStats homeTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, homeTeam);
        final TeamDescriptiveStats awayTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, awayTeam);
        if (homeTeamDescriptiveStats == null || awayTeamDescriptiveStats == null) {
            return Optional.empty();
        }

        final GoalsDescriptiveStats homeScoredStats = homeTeamDescriptiveStats.getHomeScoredStats();
        final GoalsDescriptiveStats homeConcededStats = homeTeamDescriptiveStats.getHomeConcededStats();
        final GoalsDescriptiveStats awayScoredStats = awayTeamDescriptiveStats.getAwayScoredStats();
        final GoalsDescriptiveStats awayConcededStats = awayTeamDescriptiveStats.getAwayConcededStats();

        if (homeScoredStats == null && awayConcededStats == null
                || awayScoredStats == null && homeConcededStats == null) {
            return Optional.empty();
        }

        final OptionalDouble maxScoredGoalCount = CollectionUtil.applyIfValid(Predicates.notNull(), getPercentile, homeScoredStats, awayConcededStats)
                .stream()
                .mapToDouble(Double::doubleValue)
                .max();

        final OptionalDouble maxConcededGoalCount = CollectionUtil.applyIfValid(Predicates.notNull(), getPercentile, awayScoredStats, homeConcededStats)
                .stream()
                .mapToDouble(Double::doubleValue)
                .max();

        return Optional.of(maxScoredGoalCount.getAsDouble() + maxConcededGoalCount.getAsDouble());
    }
}
