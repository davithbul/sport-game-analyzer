package com.el.robot.analyzer.analytic.measure;

import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Estimates chances based on cumulative percentiles chances.
 */
public class CumulativeProbabilityEstimator {

    public static Map<Double, Double> estimateUnderProbability(GoalsDescriptiveStats goalsDescriptiveStats) {
        final LinkedHashMap<Double, Double> underChances = new LinkedHashMap<>();
        underChances.put(5.5, goalsDescriptiveStats.getProbabilityUnder55());
        underChances.put(4.5, goalsDescriptiveStats.getProbabilityUnder45());
        underChances.put(3.5, goalsDescriptiveStats.getProbabilityUnder35());
        underChances.put(2.5, goalsDescriptiveStats.getProbabilityUnder25());
        underChances.put(1.5, goalsDescriptiveStats.getProbabilityUnder15());
        underChances.put(0.5, goalsDescriptiveStats.getProbabilityUnder05());
        return underChances;
    }

    public static Map<Double, Double> estimateOverProbabilities(GoalsDescriptiveStats goalsDescriptiveStats) {
        final LinkedHashMap<Double, Double> overProbabilities = new LinkedHashMap<>();
        overProbabilities.put(5.5, 1 - goalsDescriptiveStats.getProbabilityUnder55());
        overProbabilities.put(4.5, 1 - goalsDescriptiveStats.getProbabilityUnder45());
        overProbabilities.put(3.5, 1 - goalsDescriptiveStats.getProbabilityUnder35());
        overProbabilities.put(2.5, 1 - goalsDescriptiveStats.getProbabilityUnder25());
        overProbabilities.put(1.5, 1 - goalsDescriptiveStats.getProbabilityUnder15());
        overProbabilities.put(0.5, 1 - goalsDescriptiveStats.getProbabilityUnder05());
        return overProbabilities;
    }
}
