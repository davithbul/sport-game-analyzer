package com.el.robot.analyzer.analytic.factories;

import com.el.betting.common.Predicates;
import com.el.betting.sdk.v3.statistic.LeagueStatistic;
import com.el.betting.sdk.v3.statistic.TeamStrategy;
import com.el.betting.sdk.v3.trading.StatefulFootballTradeContext;
import com.el.robot.crawler.db.v3.LeagueStatisticManager;
import com.el.robot.crawler.db.v3.TeamStrategyManager;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
@Service
public class DrawPercentEvaluationFactory {

    private LeagueStatisticManager leagueStatisticManager;
    private TeamStrategyManager teamStrategyManager;

    @Inject
    public DrawPercentEvaluationFactory(LeagueStatisticManager leagueStatisticManager, TeamStrategyManager teamStrategyManager) {
        this.leagueStatisticManager = leagueStatisticManager;
        this.teamStrategyManager = teamStrategyManager;
    }

    /**
     * Returns map of possible predictors having key as name of predictor and value as a predictor function,
     * which gets TradeContext and returns percent of draws which it might have.
     */
    public Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> getDrawPercentEvaluatorMap() {
        Map<String, Function<StatefulFootballTradeContext, Optional<Double>>> drawPricePredictorMap = new HashMap<>();
        Function<StatefulFootballTradeContext, Optional<Double>> drawLessLeague = (tradeContext -> {
            LeagueStatistic leagueStatistic = leagueStatisticManager.find(tradeContext.getCategory(), tradeContext.getLeague());
            if(leagueStatistic == null || leagueStatistic.getStatSize() < 5) {
                return Optional.empty();
            }
            return Optional.of(leagueStatistic.getDrawPercent());
        });

        drawPricePredictorMap.put("drawLessLeague", drawLessLeague);

        Function<StatefulFootballTradeContext, Optional<Double>> drawLessTeams = (tradeContext -> {
            TeamStrategy homeTeamStrategy = teamStrategyManager.find(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam());
            TeamStrategy awayTeamStrategy = teamStrategyManager.find(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getAwayTeam());
            if(homeTeamStrategy == null || awayTeamStrategy == null ||
                    homeTeamStrategy.getHomeStatsSize() < 5 ||
                    awayTeamStrategy.getAwayStatsSize() < 5) {
                return Optional.empty();
            }

            return Optional.of(homeTeamStrategy.getDrawPercentAtHome() + awayTeamStrategy.getDrawPercentAway() / 2);
        });

        drawPricePredictorMap.put("drawLessTeams", drawLessTeams);
        drawPricePredictorMap.put("any", (tradeContext -> Optional.of(0.0)));
        return drawPricePredictorMap;
    }


    /**
     * Returns the predicate which verifies that for given predictorName the draw percent is less then
     * given maxPercentOfDraw.
     */
    public Predicate<StatefulFootballTradeContext> getDrawPercentPredicate(String predictorName, double maxPercentOfDraw) {
        switch (predictorName) {
            case "drawLessLeague":
                Function<StatefulFootballTradeContext, Optional<Double>> percentEvaluatingFunction = getDrawPercentEvaluatorMap().get(predictorName);
                return (tradeContext -> {
                    Optional<Double> percent = percentEvaluatingFunction.apply(tradeContext);
                    return percent.isPresent() && percent.get() <= maxPercentOfDraw;
                });
            case "drawLessTeams":
                return  (tradeContext -> {
                    TeamStrategy homeTeamStrategy = teamStrategyManager.find(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getHomeTeam());
                    TeamStrategy awayTeamStrategy = teamStrategyManager.find(tradeContext.getCategory(), tradeContext.getLeague(), tradeContext.getAwayTeam());
                    return homeTeamStrategy != null &&
                            awayTeamStrategy != null &&
                            homeTeamStrategy.getDrawPercentAtHome() <= maxPercentOfDraw && awayTeamStrategy.getDrawPercentAway() <= maxPercentOfDraw;
                });
            case "any":
                return Predicates.alwaysTrue();
            default:
                throw new IllegalArgumentException("Can't find draw price predictor: " + predictorName);
        }
    }
}
