package com.el.robot.analyzer.analytic.measure;

import com.el.betting.common.MathUtils;
import com.el.betting.sdk.v2.TotalType;
import com.el.betting.sdk.v3.common.OddsConverter;
import com.el.robot.analyzer.analytic.manager.PoissonProbabilityCalculator;

public class BetValueEstimator {

    public static double getBetValue(double percent, double offeredPrice) {
        return offeredPrice - OddsConverter.convertPercentToDecimal(percent);
    }

    /**
     * Returns the value of the bet. Returns the percent of chances that it will be
     * less / more than given points.
     */
    public static double getBetProbabilityByPoisson(double expectedGoalCount, TotalType totalType, double points) {
        int goalCount = ((Double) Math.floor(points)).intValue();
        if(totalType == TotalType.UNDER) {
            if(MathUtils.isInteger(points)) {
                goalCount--;
            }
            return PoissonProbabilityCalculator.getLTEGoalsProbability(expectedGoalCount, goalCount);
        } else {
            return PoissonProbabilityCalculator.getGTGoalsProbability(expectedGoalCount, goalCount);
        }
    }

    /**
     * Returns the value of the bet. Returns the percent of chances that it will be
     * less / more than given points.
     */
    public static double getBetValueByPoisson(double offeredPrice, double expectedGoalCount, TotalType totalType, double points) {
        final double betProbabilityByPoisson = getBetProbabilityByPoisson(expectedGoalCount, totalType, points);
        return getBetValue(betProbabilityByPoisson, offeredPrice);
    }
}
