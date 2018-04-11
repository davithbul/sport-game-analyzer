package com.el.robot.analyzer.analytic.measure;

import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;

import java.util.Map;

/**
 * Estimates stability and goal count based on kurtosis value.
 */
public class KurtosisEstimator {

    /**
     * Kurtosis defines stability as how tails and picks are vary.
     * If the pick is sharp and high, and tails are fare and slim than it makes stability high. otherwise low.
     *
     * F(x) = 1 - 1 / (x + 2)
     * F(0) = 0.5
     * F(1) = 2/3
     * F(-1) = 0
     */
    public static double measureStability(GoalsDescriptiveStats goalsDescriptiveStats) {
        //if > 1 it means it's pretty stable
        double kurtosis = goalsDescriptiveStats.getKurtosis();
        if (kurtosis <= 0) {
            return 0.01;
        }

        if (kurtosis < 1) {
            return kurtosis / 2;
        }

        //for number >= 1 stability = 1 - 1/(x+2)
        return 1 - 1 / (kurtosis + 2);
    }
}
