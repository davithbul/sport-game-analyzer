package com.el.robot.analyzer.analytic.manager;

import org.apache.commons.math3.distribution.PoissonDistribution;


/**
 * Calculates probability, considering that population is distributed randomly, using
 * Poisson distribution calculator.
 */
public class PoissonProbabilityCalculator {

    /**
     * Calculates the probability of having scores goalCount count of goals
     * @param averageGoalCount the average goal count scored
     * @param goalCount the probability of scoring the given amount of goals
     */
    public static double getGoalCountProbability(double averageGoalCount, int goalCount) {
        PoissonDistribution poissonDistribution = new PoissonDistribution(averageGoalCount);
        return poissonDistribution.probability(goalCount);
    }

    public static double getGoalCountNormalProbability(double averageGoalCount, int goalCount) {
        PoissonDistribution poissonDistribution = new PoissonDistribution(averageGoalCount);
        return poissonDistribution.normalApproximateProbability(goalCount);
    }

    /**
     * Returns Less than equals goal count probability
     */
    public static double getLTEGoalsProbability(double averageGoalCount, int goalCount) {
        if(averageGoalCount == 0) {
            averageGoalCount = 0.000000000000001;
        }
        PoissonDistribution poissonDistribution = new PoissonDistribution(averageGoalCount);
        return poissonDistribution.cumulativeProbability(goalCount);
    }


    /**
     * Returns Greater Than goal count probability
     */
    public static double getGTGoalsProbability(double averageGoalCount, int goalCount) {
        if(averageGoalCount == 0) {
            averageGoalCount = 0.000000000000001;
        }

        PoissonDistribution poissonDistribution = new PoissonDistribution(averageGoalCount);
        return 1 - poissonDistribution.cumulativeProbability(goalCount);
    }
}
