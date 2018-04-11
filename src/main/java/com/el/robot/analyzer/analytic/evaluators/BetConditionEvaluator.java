package com.el.robot.analyzer.analytic.evaluators;

import com.el.betting.common.Predicates;
import com.el.betting.sdk.v2.Period;
import com.el.betting.sdk.v2.betoption.api.Bet;
import com.el.betting.sdk.v3.trading.MoneyLineBettingRule;
import com.el.betting.sdk.v3.trading.StatefulFootballTradeContext;
import com.el.betting.utils.MoneyLineBetUtils;
import com.el.robot.analyzer.analytic.factories.DrawPercentEvaluationFactory;
import com.el.robot.analyzer.analytic.factories.PricePredictorFactory;
import org.apache.commons.exec.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class BetConditionEvaluator {

    public Predicate<StatefulFootballTradeContext> evaluateEventCondition(MoneyLineBettingRule bettingRule) {
        //averageGoalCount5G betValue: 0.4 MaxBackOddsShareCalculator drawLessLeague:0.3
        String[] splits = StringUtils.split(bettingRule.getDescription(), " ");
        String predictorName = splits[0];
        double minPrice = Double.parseDouble(splits[2]);
        String drawCountPredictor = splits[4].trim();
        String[] drawCountPredictorSplit = StringUtils.split(drawCountPredictor, ":");
        String drawStatsName = drawCountPredictorSplit[0];
        Double maxAllowedDrawPercent = Double.valueOf(drawCountPredictorSplit[1]);

        //drawLessLeague:0.34
        Predicate<StatefulFootballTradeContext> drawPercentPredicate = (tradeContext -> {
            if (tradeContext.getEvent().getAdditionalProperties().containsKey(drawStatsName)) {
                Double drawPercent = (Double) tradeContext.getEvent().getProperty(drawStatsName);
                return drawPercent != null && drawPercent <= maxAllowedDrawPercent;
            } else {
                throw new IllegalArgumentException(String.format("Can't find %s statistics for event %s", drawStatsName, tradeContext.getEvent().getMongoId()));
            }
        });

        return drawPercentPredicate.and(tradeContext -> {
            String key = "over05:" + predictorName;
            if (tradeContext.getEvent().getAdditionalProperties().containsKey(key)) {
                Double price = (Double) tradeContext.getEvent().getProperty(key);
                return price != null && price <= minPrice;
            } else {
                throw new IllegalArgumentException(String.format("Can't find %s statistics for event %s", key, tradeContext.getEvent().getMongoId()));
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
