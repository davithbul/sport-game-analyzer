package com.el.robot.analyzer.analytic.measure;

import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;

/**
 * shows what is the team tendency, if it's less than 0, than the most of the goals are in over than mean, otherwise under mean.
 * If skewness is less than −1 or greater than +1, the distribution is highly skewed.
 * If skewness is between −1 and −½ or between +½ and +1, the distribution is moderately skewed.
 * If skewness is between −½ and +½, the distribution is approximately symmetric.
 */
public class SkewnessEstimator {

    /**
     * Returns the team goal count tendency. if it's less than 0, than the most of the goals are in over than mean, otherwise under mean.
     * Return double value between [-1; +1]
     * If return + means has Over tendency, if return -, means has under tendency.
     * -1 means has clear under mean values, +1 means has clear over mean values
     */
    public static double measureTendency(GoalsDescriptiveStats goalsDescriptiveStats) {
        double skewness = goalsDescriptiveStats.getSkewness();
        //if it's less than 0, than the most of the goals are in over than mean, otherwise under mean
        int sign = skewness < 0 ? 1 : -1;

        skewness = Math.abs(skewness);
        if(skewness <= 1) {
            return skewness / 2 * sign;
        } else {
            return (1 - 1 / (skewness + 2)) * sign;
        }
    }
}
