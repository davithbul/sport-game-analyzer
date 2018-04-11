package com.el.robot.analyzer.analytic.manager;

import com.el.betting.sdk.v2.TimeRange;
import com.el.robot.analyzer.analytic.predictors.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * Makes goal count decision based on given
 * indicator values.
 */
@Component
public class GoalCountDecisionMaker {

    @Autowired
    private StrengthBaseGoalCountPredictor strengthBaseGoalCountPredictor;

    @Autowired
    private HeadToHeadGoalCountPredictor headToHeadGoalCountPredictor;

    @Autowired
    private AverageGoalCountPredictor averageGoalCountPredictor;

    @Autowired
    private RateBaseGoalCountPredictor rateBaseGoalCountPredictor;

    @Autowired
    private FrequencyBaseGoalCountPredictor frequencyBaseGoalCountPredictor;

    public boolean betUnder(double points, String category, String league, String homeTeam, String awayTeam, int minIndicatorCount) {
        int indicatorCount = 0;
        Optional<Double> strengthBaseGoalCount = strengthBaseGoalCountPredictor.getExpectedGoalCount(category, league, homeTeam, awayTeam, 6, null);

        if (!strengthBaseGoalCount.isPresent()) {
            strengthBaseGoalCount = strengthBaseGoalCountPredictor.getExpectedGoalCount(category, league, homeTeam, awayTeam, -1, null);
        }

        if (!strengthBaseGoalCount.isPresent()) {
            return false;
        }

        Double expectedGoalCount = strengthBaseGoalCount.get();

        if (expectedGoalCount < points) {
            indicatorCount++;
        } else {
            return false;
        }


        Optional<Double> headToHeadGoalCount = headToHeadGoalCountPredictor.getAverageGoalCount(category, league, homeTeam, awayTeam, -1, null, null);

        if (headToHeadGoalCount.isPresent()) {
            if (points - headToHeadGoalCount.get() > 0) {
                indicatorCount++;
            } else {
                return false;
            }
        }

        Optional<Double> lastGamesAvgGoalCount = averageGoalCountPredictor.getAvgGoalCount(category, league, homeTeam, awayTeam, 5);
        if (lastGamesAvgGoalCount.isPresent()) {
            if (lastGamesAvgGoalCount.get() < points) {
                indicatorCount++;
            } else {
                return false;
            }
        }

        Optional<Double> rateBaseAvgGoalCount = rateBaseGoalCountPredictor.getExpectedGoalCount(category, league, homeTeam, awayTeam, 5, null);
        if (rateBaseAvgGoalCount.isPresent()) {
            if (rateBaseAvgGoalCount.get() < points) {
                indicatorCount++;
            } else {
                return false;
            }
        }

        //estimate poisson
        if (strengthBaseGoalCount.get() > 0) {
            double probability = PoissonProbabilityCalculator.getLTEGoalsProbability(strengthBaseGoalCount.get(), (int) (points - 0.5));
            if (probability > 0.25) {
                indicatorCount++;
            } else {
                return false;
            }
        }

        Optional<Double> homeTeamOverCountFrequency = frequencyBaseGoalCountPredictor.getOverGoalCountFrequency(points, null, category, league, homeTeam, 5);
        Optional<Double> awayTeamOverCountFrequency = frequencyBaseGoalCountPredictor.getOverGoalCountFrequency(points, null, category, league, awayTeam, 5);
        if (homeTeamOverCountFrequency.isPresent() && awayTeamOverCountFrequency.isPresent()) {
            if (((homeTeamOverCountFrequency.get() + awayTeamOverCountFrequency.get()) / 2) < 0.5) {
                indicatorCount++;
            } else {
                return false;
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastYear = now.minusYears(1);
        LocalDateTime startOfMonth = lastYear.with(firstDayOfMonth()).withHour(0).withMinute(0);
        LocalDateTime endOfMonth = lastYear.with(lastDayOfMonth()).withHour(23).withMinute(59);
        Optional<Double> lastYearGoalCountPredictor = averageGoalCountPredictor.getAvgGoalCount(category, league, homeTeam, awayTeam, TimeRange.of(startOfMonth, endOfMonth));
        if (lastYearGoalCountPredictor.isPresent()) {
            if (lastYearGoalCountPredictor.get() < points) {
                indicatorCount++;
            } else {
                return false;
            }
        }

        return indicatorCount >= minIndicatorCount;
    }
}
