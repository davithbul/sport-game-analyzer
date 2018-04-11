package com.el.robot.analyzer.analytic.manager;

import com.el.betting.common.MathUtils;
import com.el.betting.common.OptionalElements;
import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;
import com.el.betting.sdk.v3.statistic.TeamDescriptiveStats;
import com.el.robot.analyzer.analytic.measure.*;
import com.el.robot.crawler.db.v3.TeamDescriptiveStatsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.el.betting.common.OptionalElements.getFirstPresent;

@Component
public class TeamStabilityMeasure {

    @Autowired
    private TeamDescriptiveStatsManager teamDescriptiveStatsManager;

    /**
     * When mean and median are close, than distribution is normal, but it doesn't mean anything
     * if modeFrequency is >= 0.5 it means the team scores the same goal count usually, and it's pretty stable
     * if |skewness| < 0.35 it means distribution is close to normal
     * stdDeviation shows how spread numbers are, if it's less than < 1, it means numbers are pretty close, and team is pretty stable
     * if percentile99 - percentile70 < 0.3 it means there are concentration in right tail, and team is stable but in case for over betting
     * if 2 * (percentile99 - median) < median it means distribution is in right tail, and team is stable in scoring goals between median -  percentile99
     * if (percentile99 - median) > 2 * median it means distribution is in left tail, good situation for under betting, and team is stable in scoring goals between 0 -  median
     * if there are more than 1 modes, it mean graph has more than one peak, and it is unstable
     * if there are more than 1 modes, and mode differences are less than 1, it means team is stable between those modes
     * If Kurtosis is higher than 1, it means there is sharp peak and long and rare tail. So the peaks are stable for betting.
     * If there is 2 high peaks, which are pretty close by frequency but not close by goal count, that it sign of unstability.
     */
    public Optional<Double> getTendencyIfStable(String category, String league, String homeTeam, String awayTeam, boolean strict) {
        final TeamDescriptiveStats homeTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, homeTeam);
        final TeamDescriptiveStats awayTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, awayTeam);
        if(homeTeamDescriptiveStats == null || awayTeamDescriptiveStats == null) {
            return Optional.empty();
        }
        if(isStable(homeTeamDescriptiveStats, awayTeamDescriptiveStats, strict)) {
            return getTendency(homeTeamDescriptiveStats, awayTeamDescriptiveStats, strict);
        }
        return Optional.empty();
    }

    public boolean isStable(String category, String league, String homeTeam, String awayTeam, boolean strict) {
        final TeamDescriptiveStats homeTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, homeTeam);
        final TeamDescriptiveStats awayTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, awayTeam);
        if(homeTeamDescriptiveStats == null || awayTeamDescriptiveStats == null) {
            return false;
        }
        return isStable(homeTeamDescriptiveStats, awayTeamDescriptiveStats, strict);
    }

    private boolean isStable(TeamDescriptiveStats homeTeamDescriptiveStats, TeamDescriptiveStats awayTeamDescriptiveStats, boolean strict) {
        final GoalsDescriptiveStats homeScoredStats = homeTeamDescriptiveStats.getHomeScoredStats();
        final GoalsDescriptiveStats homeConcededStats = homeTeamDescriptiveStats.getHomeConcededStats();
        final GoalsDescriptiveStats awayScoredStats = awayTeamDescriptiveStats.getAwayScoredStats();
        final GoalsDescriptiveStats awayConcededStats = awayTeamDescriptiveStats.getAwayConcededStats();

        return isStable(homeScoredStats, strict)
                && isStable(awayScoredStats, strict)
                && (!strict || isStable(homeConcededStats, strict) && isStable(awayConcededStats, strict));
    }

    private boolean isStable(GoalsDescriptiveStats goalsDescriptiveStats, boolean strict) {
        double goalFrequencyStability = GoalFrequencyEstimator.measureStability(goalsDescriptiveStats);
        double stdStability = StdDeviationEstimator.measureStability(goalsDescriptiveStats);
        double kurtosisStability = KurtosisEstimator.measureStability(goalsDescriptiveStats);
        return goalFrequencyStability >= 0.5
                && stdStability >= 0.5
                && (!strict || kurtosisStability >= 0.5);
    }

    public Optional<Double> getTendency(String category, String league, String homeTeam, String awayTeam, boolean strict) {
        final TeamDescriptiveStats homeTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, homeTeam);
        final TeamDescriptiveStats awayTeamDescriptiveStats = teamDescriptiveStatsManager.find(category, league, awayTeam);
        if(homeTeamDescriptiveStats == null || awayTeamDescriptiveStats == null) {
            return Optional.empty();
        }
        return getTendency(homeTeamDescriptiveStats, awayTeamDescriptiveStats, strict);
    }

    protected Optional<Double> getTendency(TeamDescriptiveStats homeTeamDescriptiveStats, TeamDescriptiveStats awayTeamDescriptiveStats, boolean strict) {
        final GoalsDescriptiveStats homeScoredStats = homeTeamDescriptiveStats.getHomeScoredStats();
        final GoalsDescriptiveStats homeConcededStats = homeTeamDescriptiveStats.getHomeConcededStats();
        final GoalsDescriptiveStats awayScoredStats = awayTeamDescriptiveStats.getAwayScoredStats();
        final GoalsDescriptiveStats awayConcededStats = awayTeamDescriptiveStats.getAwayConcededStats();

        final Optional<Double> homeScoreTendency = getTendency(homeScoredStats, strict);
        final Optional<Double> homeConcedeTendency = getTendency(homeConcededStats, strict);
        final Optional<Double> awayScoreTendency = getTendency(awayScoredStats, strict);
        final Optional<Double> awayConcedeTendency = getTendency(awayConcededStats, strict);

        //check if all stats are available and all have the same tendency
        if (strict) {
            if (homeScoreTendency.isPresent() && homeConcedeTendency.isPresent()
                    && awayScoreTendency.isPresent() && awayConcedeTendency.isPresent()
                    && MathUtils.signMatches(homeScoreTendency.get(), homeConcedeTendency.get(), awayScoreTendency.get(), awayConcedeTendency.get())) {
                if (Math.signum(homeConcedeTendency.get()) > 0) {//it's over tendency
                    final double overGoalCount =
                            Math.min(homeScoreTendency.get(), awayConcedeTendency.get())
                                    +
                                    Math.min(homeConcedeTendency.get(), awayScoreTendency.get());
                    return Optional.of(overGoalCount);
                } else {//it's under tendency
                    final double underGoalCount =
                            Math.min(homeScoreTendency.get(), awayConcedeTendency.get())
                                    +
                                    Math.min(homeConcedeTendency.get(), awayScoreTendency.get());
                    return Optional.of(underGoalCount);
                }
            }
        } else {
            //one of each teams stats is available
            if ((homeScoreTendency.isPresent() || homeConcedeTendency.isPresent())
                    && (awayScoreTendency.isPresent() || awayConcedeTendency.isPresent())) {
                final List<Double> homeScoreTendencies = OptionalElements.applyIfPresent((value -> value), homeScoreTendency, awayConcedeTendency);
                final List<Double> homeConcedeTendencies = OptionalElements.applyIfPresent((value -> value), homeConcedeTendency, awayScoreTendency);
                final List<Double> signs = OptionalElements.applyIfPresent(Math::signum, homeScoreTendency, homeConcedeTendency, awayScoreTendency, awayConcedeTendency);
                final Double defaultSign = signs.get(0);
                final boolean allSignMatch = signs.stream().allMatch(sign -> Objects.equals(defaultSign, sign) || sign == 0);
                if (allSignMatch) {
                    if (defaultSign > 0) {//it's over tendency
                        final Optional<Double> homeScoreMin = homeScoreTendencies.stream().min(Double::compare);
                        final Optional<Double> homeConcedeMin = homeConcedeTendencies.stream().min(Double::compare);
                        final double overGoalCount = getFirstPresent(homeScoreMin, homeConcedeMin) + getFirstPresent(homeConcedeMin, homeScoreMin);
                        return Optional.of(overGoalCount);
                    } else {//it's under tendency
                        final Optional<Double> homeScoreMax = homeScoreTendencies.stream().min(Double::compare);
                        final Optional<Double> homeConcedeMax = homeConcedeTendencies.stream().min(Double::compare);
                        final double underGoalCount = getFirstPresent(homeScoreMax, homeConcedeMax) + getFirstPresent(homeConcedeMax, homeScoreMax);
                        return Optional.of(underGoalCount);
                    }
                } else {//if all signs are not matching, than we can find
                    //there is 2 possible scenarios,
                    // a) in one where there is intersection. E.G. (1+ and 4-) has intersection (1; 4);
                    // b) if there is no intersection. E.G. (1- and 4+); Than we can say that it would be more or equal than 1
                    Optional<Double> homeScoreMin = Optional.empty();
                    if(!homeScoreTendencies.isEmpty()) {
                        final boolean homeScoreSignMatches = signs.stream().allMatch(sign -> Objects.equals(homeScoreTendencies.get(0), sign));
                        if(homeScoreSignMatches) {
                            homeScoreMin = homeScoreTendencies.stream().map(Math::abs).min(Double::compare);
                        } else {
                            //if homeScoreMin sign < 0 there is no intersection
                            homeScoreMin = homeScoreTendencies.stream().map(Math::abs).min(Double::compare);
                        }
                    }
                    final Optional<Double> homeConcedeMin = homeConcedeTendencies.stream().map(Math::abs).min(Double::compare);
                    final double overGoalCount = getFirstPresent(homeScoreMin, homeConcedeMin) + getFirstPresent(homeConcedeMin, homeScoreMin);
                    return Optional.of(overGoalCount);
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Returns tendency, if returns +1.5, it means there will be more or equal than 1.5 goals.
     * If returns -1.5 it means there will be less or equal than 1.5 goals.
     * if returns 0, it means no goals are expected
     */
    public Optional<Double> getTendency(GoalsDescriptiveStats goalsDescriptiveStats, boolean strict) {
        final double borderWideness = MedianEstimator.measureBorderWideness(goalsDescriptiveStats);
        final double skewnessTendency = SkewnessEstimator.measureTendency(goalsDescriptiveStats);
        final double percentileBaseTendency = PercentileBaseEstimator.measureTendencyOverMedian(goalsDescriptiveStats);

        //check that all have the same sign all the signs are the same
        if(!MathUtils.signMatches(skewnessTendency, percentileBaseTendency)) {
            return Optional.empty();
        }

        //get signs of tendencies
        final double signum = Math.signum(skewnessTendency);

        if (Math.abs(skewnessTendency) >= 0.25
                && Math.abs(percentileBaseTendency) >= 0.5) {
            //has clear tendency                               `
            return Optional.of(signum * goalsDescriptiveStats.getMedian());
        } else {
            return Optional.empty();
        }
    }
}
