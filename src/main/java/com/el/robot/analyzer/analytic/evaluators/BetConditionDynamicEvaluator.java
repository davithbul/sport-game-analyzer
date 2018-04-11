package com.el.robot.analyzer.analytic.evaluators;

import com.el.betting.common.Predicates;
import com.el.betting.sdk.v2.Period;
import com.el.betting.sdk.v2.TotalType;
import com.el.betting.sdk.v2.betoption.api.Bet;
import com.el.betting.sdk.v3.trading.MoneyLineBettingRule;
import com.el.betting.sdk.v3.trading.StatefulFootballTradeContext;
import com.el.betting.utils.MoneyLineBetUtils;
import com.el.robot.analyzer.analytic.factories.DrawPercentEvaluationFactory;
import com.el.robot.analyzer.analytic.factories.PricePredictorFactory;
import org.apache.commons.exec.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BetConditionDynamicEvaluator extends BetConditionEvaluator{

    private DrawPercentEvaluationFactory drawPercentEvaluationFactory;
    private PricePredictorFactory pricePredictorFactory;

    @Autowired
    public BetConditionDynamicEvaluator(DrawPercentEvaluationFactory drawPercentEvaluationFactory, PricePredictorFactory pricePredictorFactory) {
        this.drawPercentEvaluationFactory = drawPercentEvaluationFactory;
        this.pricePredictorFactory = pricePredictorFactory;
    }

    public Predicate<StatefulFootballTradeContext> evaluateEventCondition(MoneyLineBettingRule bettingRule) {
        //averageGoalCount5G betValue: 0.4 MaxBackOddsShareCalculator drawLessLeague:0.3
        String[] splits = StringUtils.split(bettingRule.getDescription(), " ");
        String predictorName = splits[0];
        double minPrice = Double.parseDouble(splits[2]);
        String drawCountPredictor = splits[4].trim();
        String[] drawCountPredictorSplit = StringUtils.split(drawCountPredictor, ":");

        //drawLessLeague:0.34
        Predicate<StatefulFootballTradeContext> drawPercentPredicate = (tradeContext -> {
            if (tradeContext.getEvent().getAdditionalProperties().containsKey(drawCountPredictorSplit[0])) {
                Double drawPercent = (Double) tradeContext.getEvent().getProperty(drawCountPredictorSplit[0]);
                return drawPercent != null && drawPercent <= Double.parseDouble(drawCountPredictorSplit[1]);
            } else {
                return drawPercentEvaluationFactory.getDrawPercentPredicate(drawCountPredictorSplit[0], Double.parseDouble(drawCountPredictorSplit[1]))
                        .test(tradeContext);

            }
        });

        return drawPercentPredicate.and(tradeContext -> {
            String key = "over05:" + predictorName;
            if (tradeContext.getEvent().getAdditionalProperties().containsKey(key)) {
                Double price = (Double) tradeContext.getEvent().getProperty(key);
                return price != null && price <= minPrice;
            } else {
                Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> predictorsProbabilityFunctions = pricePredictorFactory.getPredictorsProbability(predictorName, 0.5, TotalType.OVER, null);
                return predictorsProbabilityFunctions
                        .entrySet()
                        .stream()
                        .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().apply(tradeContext)))
                        .allMatch(predicate -> predicate.getValue().isPresent()
                                && predicate.getValue().get() <= minPrice);
            }
        });
    }

    public Predicate<StatefulFootballTradeContext> getBackBetCondition(MoneyLineBettingRule bettingRule) {
        return  (tradeContext -> {
            Bet layBet = (Bet) tradeContext.getValue("layBet");
            return MoneyLineBetUtils.getBackPrice(tradeContext.getEvent(), Period.MATCH, bettingRule.getSide()).compareTo(layBet.getPrice()) > 0;
        });
    }

    public Predicate<StatefulFootballTradeContext> getLayCondition(MoneyLineBettingRule bettingRule) {
        return Predicates.alwaysTrue();
    }
}
