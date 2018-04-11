package com.el.robot.analyzer.analytic.measure;

import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;

/**
 * Makes estimation based on percentiles
 * if percentile99 - percentile70 < 0.3 it means there are mass data in the right tail, and team is stable but in case for over betting
 * if 2 * (percentile99 - median) < median it means distribution is in right tail, and team is stable in scoring goals between median - percentile99
 * if (percentile99 - median) > 2 * median it means distribution is in left tail, good situation for under betting, and team is stable in scoring goals between 0 -  median
 */
public class PercentileBaseEstimator {

    /**
     * Returns how many times the goal count over / under the median is higher than the opposite side of median
     * Return double value between [-1; +1]
     * If return + means has Over tendency, if return -, means has under tendency.
     * -1 means has clear under mean values, +1 means has clear over mean values
     */
    public static double measureTendencyOverMedian(GoalsDescriptiveStats goalsDescriptiveStats) {
        final double median = goalsDescriptiveStats.getMedian();
        final double difference = goalsDescriptiveStats.getPercentile95() - median;
        //if difference is small, it means there are mass goal data between median to max edge
        return (median - difference) / median;
    }
}
