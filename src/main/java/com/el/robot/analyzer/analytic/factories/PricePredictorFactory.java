package com.el.robot.analyzer.analytic.factories;

import com.el.betting.sdk.v2.TimeRange;
import com.el.betting.sdk.v2.TotalType;
import com.el.betting.sdk.v3.common.OddsConverter;
import com.el.betting.sdk.v3.trading.StatefulFootballTradeContext;
import com.el.betting.sdk.v3.trading.FootballTradeContext;
import com.el.robot.analyzer.analytic.measure.BetValueEstimator;
import com.el.robot.analyzer.analytic.predictors.*;
import org.apache.commons.exec.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.el.betting.sdk.v2.TotalType.OVER;
import static com.el.betting.utils.TotalPointsUtils.getBackPrice;
import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@SuppressWarnings("unchecked")
@Service
public class PricePredictorFactory {

    private RateBaseGoalCountPredictor rateBaseGoalCountPredictor;
    private AverageGoalCountPredictor averageGoalCountPredictor;
    private FrequencyBaseGoalCountPredictor frequencyBaseGoalCountPredictor;
    private HeadToHeadGoalCountPredictor headToHeadGoalCountPredictor;
    private StrengthBaseGoalCountPredictor strengthBaseGoalCountPredictor;

    @Inject
    public PricePredictorFactory(RateBaseGoalCountPredictor rateBaseGoalCountPredictor, AverageGoalCountPredictor averageGoalCountPredictor, FrequencyBaseGoalCountPredictor frequencyBaseGoalCountPredictor, HeadToHeadGoalCountPredictor headToHeadGoalCountPredictor, StrengthBaseGoalCountPredictor strengthBaseGoalCountPredictor) {
        this.rateBaseGoalCountPredictor = rateBaseGoalCountPredictor;
        this.averageGoalCountPredictor = averageGoalCountPredictor;
        this.frequencyBaseGoalCountPredictor = frequencyBaseGoalCountPredictor;
        this.headToHeadGoalCountPredictor = headToHeadGoalCountPredictor;
        this.strengthBaseGoalCountPredictor = strengthBaseGoalCountPredictor;
    }

    /**
     * Returns map of possible predictors having key as name of predictor and value as a predictor function,
     * which gets TradeContext and returns predicted goal count over/ under probability.
     */
    public Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> getPricePredictorsMap(double points, TotalType totalType, TimeRange<Integer> goalPeriod) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastYear = now.minusYears(1);
        LocalDateTime lastYearStartOfMonth = lastYear.with(firstDayOfMonth()).withHour(0).withMinute(0);
        LocalDateTime lastYearEndOfMonth = lastYear.with(lastDayOfMonth()).withHour(23).withMinute(59);
        final LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMinutes(3);
        final LocalDateTime oneMonthAgo = LocalDateTime.now().minusMinutes(2);


        Map<String, Function<FootballTradeContext, Optional<Double>>> goalCountPredictionMap = new LinkedHashMap<>();
        goalCountPredictionMap.put("averageGoalCount5G",
                (tradeContext -> averageGoalCountPredictor.getAvgGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), true, 5, goalPeriod, null)));

        goalCountPredictionMap.put("averageGoalCount10G",
                (tradeContext -> averageGoalCountPredictor.getAvgGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), true, 10, goalPeriod, null)));

//        goalCountPredictionMap.put("averageGoalCountTwoMonthAgo",
//                (tradeContext -> averageGoalCountPredictor.getAvgGoalCount(tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), TimeRange.after(twoMonthsAgo))));
//
//        goalCountPredictionMap.put("averageGoalCountOneMonthAgo",
//                (tradeContext -> averageGoalCountPredictor.getAvgGoalCount(tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), TimeRange.after(oneMonthAgo))));

        goalCountPredictionMap.put("averageGoalCountLastYear",
                (tradeContext -> averageGoalCountPredictor.getAvgGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), TimeRange.of(lastYearStartOfMonth, lastYearEndOfMonth), goalPeriod)));

        goalCountPredictionMap.put("rateBaseGoalCount5G",
                (tradeContext -> rateBaseGoalCountPredictor.getExpectedGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

        goalCountPredictionMap.put("rateBaseGoalCount10G",
                (tradeContext -> rateBaseGoalCountPredictor.getExpectedGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

        goalCountPredictionMap.put("headToHeadAvg5G",
                (tradeContext -> headToHeadGoalCountPredictor.getAverageGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

        goalCountPredictionMap.put("headToHeadAvg10G",
                (tradeContext -> headToHeadGoalCountPredictor.getAverageGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

        goalCountPredictionMap.put("headToHeadAvgIgnoreSide5G",
                (tradeContext -> headToHeadGoalCountPredictor.getAverageGoalCountIgnoreSide(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

        goalCountPredictionMap.put("headToHeadAvgIgnoreSide10G",
                (tradeContext -> headToHeadGoalCountPredictor.getAverageGoalCountIgnoreSide(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

        if (goalPeriod == null) {
            goalCountPredictionMap.put("headToHeadGoalCount5G",
                    (tradeContext -> headToHeadGoalCountPredictor.getExpectedGoalCountStrengthBase(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

            goalCountPredictionMap.put("headToHeadGoalCount10G",
                    (tradeContext -> headToHeadGoalCountPredictor.getExpectedGoalCountStrengthBase(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

            goalCountPredictionMap.put("headToHeadGoalCountIgnoreSide5G",
                    (tradeContext -> headToHeadGoalCountPredictor.getExpectedGoalCountIgnoreSide(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

            goalCountPredictionMap.put("headToHeadGoalCountIgnoreSide10G",
                    (tradeContext -> headToHeadGoalCountPredictor.getExpectedGoalCountIgnoreSide(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

            goalCountPredictionMap.put("strengthBaseExpectedGoalCount5G",
                    (tradeContext -> strengthBaseGoalCountPredictor.getExpectedGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, TimeRange.before(tradeContext.getBeforeEventStartTime()))));

            goalCountPredictionMap.put("strengthBaseExpectedGoalCount10G",
                    (tradeContext -> strengthBaseGoalCountPredictor.getExpectedGoalCount(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, TimeRange.before(tradeContext.getBeforeEventStartTime()))));
        }

        Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> pricePredictionMap = new LinkedHashMap<>();
        for (Map.Entry<String, Function<FootballTradeContext, Optional<Double>>> entry : goalCountPredictionMap.entrySet()) {
            String name = entry.getKey();
            Function<FootballTradeContext, Optional<Double>> pricePredictor = entry.getValue();
            pricePredictionMap.put(name,
                    (tradeContext -> {
                        Optional<Double> value = (Optional<Double>) tradeContext.getValue(name);
                        if (value == null) {
                            value = pricePredictor.apply(tradeContext);
                            tradeContext.recordValue(name, value);
                        }

                        if (value.isPresent()) {
                            final double betProbabilityByPoisson = BetValueEstimator.getBetProbabilityByPoisson(value.get(), totalType, points);
                            return Optional.of(OddsConverter.convertPercentToDecimal(betProbabilityByPoisson));
                        } else {
                            return Optional.empty();
                        }
                    }));
        }

        pricePredictionMap.put("frequencyBaseGoalCount5G",
                (tradeContext -> {
                    Optional<Double> value = (Optional<Double>) tradeContext.getValue("frequencyBaseGoalCount5G");
                    if (value == null) {
                        value = frequencyBaseGoalCountPredictor.getAvgOverGoalCountFrequency(points, tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, false, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()));
                        tradeContext.recordValue("frequencyBaseGoalCount5G", value);
                    }
                    if (value.isPresent()) {
                        return Optional.of(OddsConverter.convertPercentToDecimal(totalType == OVER ? value.get() : 1 - value.get()));
                    }
                    return Optional.empty();
                }));

        pricePredictionMap.put("frequencyBaseGoalCount10G",
                (tradeContext -> {
                    Optional<Double> value = (Optional<Double>) tradeContext.getValue("frequencyBaseGoalCount10G");
                    if (value == null) {
                        value = frequencyBaseGoalCountPredictor.getAvgOverGoalCountFrequency(points, tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, false, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()));
                        tradeContext.recordValue("frequencyBaseGoalCount10G", value);
                    }

                    if (value.isPresent()) {
                        return Optional.of(OddsConverter.convertPercentToDecimal(totalType == OVER ? value.get() : 1 - value.get()));
                    }
                    return Optional.empty();
                }));

        pricePredictionMap.put("frequencyBaseGoalCountIgnoreSide5G",
                (tradeContext -> {
                    Optional<Double> value = (Optional<Double>) tradeContext.getValue("frequencyBaseGoalCountIgnoreSide5G");
                    if (value == null) {
                        value = frequencyBaseGoalCountPredictor.getAvgOverGoalCountFrequency(points, tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 5, true, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()));
                        tradeContext.recordValue("frequencyBaseGoalCountIgnoreSide5G", value);
                    }

                    if (value.isPresent()) {
                        return Optional.of(OddsConverter.convertPercentToDecimal(totalType == OVER ? value.get() : 1 - value.get()));
                    }
                    return Optional.empty();
                }));

        pricePredictionMap.put("frequencyBaseGoalCountIgnoreSide10G",
                (tradeContext -> {
                    Optional<Double> value = (Optional<Double>) tradeContext.getValue("frequencyBaseGoalCountIgnoreSide10G");
                    if (value == null) {
                        value = frequencyBaseGoalCountPredictor.getAvgOverGoalCountFrequency(points, tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam(), tradeContext.getAwayTeam(), 10, true, goalPeriod, TimeRange.before(tradeContext.getBeforeEventStartTime()));
                        tradeContext.recordValue("frequencyBaseGoalCountIgnoreSide10G", value);
                    }
                    if (value.isPresent()) {
                        return Optional.of(OddsConverter.convertPercentToDecimal(totalType == OVER ? value.get() : 1 - value.get()));
                    }
                    return Optional.empty();
                }));

        pricePredictionMap.put("Any", (tradeContext -> Optional.of(0.5d)));

        return pricePredictionMap;
    }


    /**
     * Returns the predictor function which has given name
     */
    public Function<StatefulFootballTradeContext, Optional<Double>> getPredictorProbability(String predictorName, double points, TotalType totalType, TimeRange<Integer> goalPeriod) {
        return getPricePredictorsMap(points, totalType, goalPeriod).get(predictorName);
    }

    /**
     * Returns map with the name of predictor and the predictor implementation, which will predict the odd of the outcome.
     */
    public Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> getPredictorsProbability(String predictorName, double points, TotalType totalType, TimeRange<Integer> goalPeriod) {
        Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> predictorProbabilities = new HashMap<>();
        Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> predictorsProbabilityMap = getPricePredictorsMap(points, totalType, goalPeriod);
        String[] andPredictors = StringUtils.split(predictorName, " && ");
        for (String predictorFunctionName : andPredictors) {
            predictorProbabilities.put(predictorFunctionName, predictorsProbabilityMap.get(predictorFunctionName));
        }
        return predictorProbabilities;
    }

    /**
     * returns map of predictor name and predicate which will return true if value between offered and predicted price is at least by given minBetValue size.
     */
    public Map<String, Predicate<StatefulFootballTradeContext>> getValuePredicateMap(double points, TotalType totalType, TimeRange<Integer> goalPeriod, double minBetValue) {
        Map<String, Predicate<StatefulFootballTradeContext>> backPredicateMap = new LinkedHashMap<>();
        Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> pricePredictionMap = getPricePredictorsMap(points, totalType, goalPeriod);
        for (Map.Entry<String, Function<StatefulFootballTradeContext, Optional<Double>>> entry : pricePredictionMap.entrySet()) {
            Predicate<StatefulFootballTradeContext> predicate = (tradeContext -> {
                final Optional<Double> predictedPrice = entry.getValue().apply(tradeContext);
                if (predictedPrice.isPresent()) {
                    BigDecimal offeredPrice = getBackPrice(tradeContext.getEvent(), points, totalType);
                    return offeredPrice.doubleValue() - predictedPrice.get() >= minBetValue;
                }
                return false;
            });
            backPredicateMap.put(entry.getKey(), predicate);
        }
        return backPredicateMap;
    }

    public Predicate<StatefulFootballTradeContext> getValuePredicate(String predictorName, double points, TotalType totalType, TimeRange<Integer> goalPeriod, double minBetValue) {
        Map<String, Predicate<StatefulFootballTradeContext>> valuePredicateMap = getValuePredicateMap(points, totalType, goalPeriod, minBetValue);
        String[] andPredictors = StringUtils.split(predictorName, " && ");
        Predicate<StatefulFootballTradeContext> andPredictor = valuePredicateMap.get(andPredictors[0]);
        for (int i = 1; i < andPredictors.length; i++) {
            andPredictor = andPredictor.and(valuePredicateMap.get(andPredictors[i]));
        }

        return andPredictor;
    }
}
