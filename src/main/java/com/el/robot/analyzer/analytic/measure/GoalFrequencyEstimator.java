package com.el.robot.analyzer.analytic.measure;

import com.el.betting.sdk.v3.statistic.GoalsDescriptiveStats;

import java.util.Iterator;
import java.util.Map;

/**
 * Measures stability and goal count based on goal count frequency.
 */
public class GoalFrequencyEstimator {

    /**
     * Returns coefficient within 0 - 1, which describes the stability level of team goal count.
     * If 1, it's very stable, if 0, it's unstable
     *
     * It's checks stability, based on how many times the most frequent goal count is occurring, and how
     * close are the most frequent and second most frequent goal count. As close they are as stable it is.
     */
    public static double measureStability(GoalsDescriptiveStats goalsDescriptiveStats) {
        double measuredValue = 0;
        final Iterator<Map.Entry<Integer, Double>> entryIterator = goalsDescriptiveStats.getGoalFrequencies().entrySet().iterator();
        final Map.Entry<Integer, Double> highestPick = entryIterator.next();
        if(!entryIterator.hasNext()) {//if team always score the same goal count
            return highestPick.getValue();
        }

        final Map.Entry<Integer, Double> secondHighestPick = entryIterator.next();
        if(highestPick.getValue() >= 0.8) { //if highest pick is very frequent, and sum it up
            measuredValue += highestPick.getValue();
        } else if(highestPick.getValue() >= 0.5) {
            measuredValue += highestPick.getValue();
            //if second highest goal count is close to highest goal count, than that's is a good sign.
            if(Math.abs(highestPick.getKey() - secondHighestPick.getKey()) <= 1) {
                measuredValue += Math.min(secondHighestPick.getValue(), 0.3);
            }
        } else if(Math.abs(highestPick.getKey() - secondHighestPick.getKey()) <= 1) { //if 2 picks goal count are close
            measuredValue += (highestPick.getValue() + highestPick.getValue()) * 0.8;
        } else {
            measuredValue = highestPick.getValue();
        }
        return measuredValue;
    }

    /**
     * Estimates chances of each possible outcome.
     * The returned maps contains goals and it's happening possibility within 0 - 1.
     * 0 - means no chance, 1 - means 100% chance.
     */
    public static Map<Integer, Double> estimateChances(GoalsDescriptiveStats goalsDescriptiveStats) {
        return goalsDescriptiveStats.getGoalFrequencies();
    }
}
