package com.el.robot.analyzer.analytic.measure;

import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;

/**
 * Estimates values based on standard deviation.
 */
public class StdDeviationEstimator {

    /**
     * Returns value between [0; 1] which is the estimation of stability.
     * If stdDev is small number, it means the goals are pretty close to pick,
     * and the team is stable.
     * The measure function is following
     * <p>
     * F(x) = 1 / (x + 1)
     * f(0) = 1
     * f(1) = 0.5
     * f(+∞) = 0
     * </p>
     */
    public static double measureStability(GoalsDescriptiveStats goalsDescriptiveStats) {
        return 1 / (1 + goalsDescriptiveStats.getStdDeviation());
    }
}
