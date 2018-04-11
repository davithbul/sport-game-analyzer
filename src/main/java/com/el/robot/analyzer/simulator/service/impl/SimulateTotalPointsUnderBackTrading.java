//package com.el.robot.analyzer.simulator.service.impl;
//
//import com.el.betting.sdk.v2.betoption.bettype.layback.LayBackBetOption;
//import com.el.betting.sdk.v2.betoption.bettype.totalpoints.TotalPointsBetOption;
//import com.el.betting.sdk.v3.betoption.group.LayBackBetOptionGroup;
//import com.el.betting.sdk.v3.betoption.group.LayBackBetOptionGroupShare;
//import com.el.robot.crawler.db.v3.EventStatistic;
//import com.el.robot.analyzer.simulator.service.SimulationService;
//import com.el.robot.bet.trading.model.TradeBetOptionContext;
//import com.el.robot.bet.trading.rule.UnderBackBettingRule;
//import com.el.robot.bet.trading.util.EventAnalyzer;
//import com.el.robot.calculator.services.impl.outcome.layback.LayBackOddsShareCalculator;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.List;
//import java.util.Optional;
//
//import static com.el.betting.sdk.v2.BetExchangeType.BACK;
//import static com.el.betting.sdk.v2.BetExchangeType.LAY;
//import static com.el.betting.sdk.v2.TotalType.UNDER;
//
///**
// * Match should be at 0-0 till 55-57th minute of the game and if, odds in under 1.5 market are above 1.37
// * Start your trade by backing the market Under 1.5  first.
// * Exit point of your trade should be by 61th or 62nd minute (it depends on the league)
// * If no team scores any goal up to exit point, you should continue your trade where you will make 5-10% profit on your stake.
// */
//public class SimulateTotalPointsUnderBackTrading implements SimulationService<UnderBackBettingRule, TotalPointsBetOption> {
//
//    private final LayBackOddsShareCalculator oddsShareCalculator;
//
//    public SimulateTotalPointsUnderBackTrading(LayBackOddsShareCalculator oddsShareCalculator) {
//        this.oddsShareCalculator = oddsShareCalculator;
//    }
//
//    public Optional<BigDecimal> calculatePnL(List<TotalPointsBetOption> betOptionList, EventStatistic eventStatistic, UnderBackBettingRule simulationRule) {
//        double points = simulationRule.getPoints();
//
//        Optional<TotalPointsBetOption> backBetOptionOptional = betOptionList.stream()
//                .filter(betOption -> EventAnalyzer.isOddsNormalized(betOptionList, (int) betOption.getProperty("currentMinute"),
//                        simulationRule.getPoints(), UNDER, simulationRule.getBackBetAllowedDiff()))
//                .filter(betOption -> simulationRule.getBackBetCondition().test(new TradeBetOptionContext(betOption)))
//                .filter(betOption -> {
//                    long currentMinute = (long) betOption.getProperty("currentMinute");
//                    return currentMinute >= simulationRule.getBackBetStartTime() && currentMinute <= simulationRule.getBackBetEndTime();
//                })
//                .filter(betOption -> betOption.getPrice().compareTo(simulationRule.getMinBackPrice()) >= 0)
//                .findFirst();
//
//        if (!backBetOptionOptional.isPresent()) {
//            return Optional.empty();
//        }
//
//        //now looking for lay bet option
//        Optional<TotalPointsBetOption> layBetOptionOptional = betOptionList.stream()
//                .filter(betOption -> (long) betOption.getProperty("currentMinute") >= simulationRule.getLayBetStartTime())
//                .filter(betOption -> (long) betOption.getProperty("currentMinute") <= simulationRule.getLayBetEndTime())
//                .filter(betOption -> EventAnalyzer.isOddsNormalized(betOptionList, (int) betOption.getProperty("currentMinute"),
//                        simulationRule.getPoints(), UNDER, simulationRule.getLayBetAllowedDiff()))
//                .findFirst();
//
//        if (layBetOptionOptional.isPresent()) {
//            LayBackBetOption backBetOption = new LayBackBetOption(backBetOptionOptional.get(), BACK, backBetOptionOptional.get().getPrice()) ;
//            LayBackBetOption layBetOption = new LayBackBetOption(layBetOptionOptional.get(), LAY, layBetOptionOptional.get().getPrice());
//            LayBackBetOptionGroup layBackBetOptionGroup = new LayBackBetOptionGroup<LayBackBetOption>(backBetOption, layBetOption);
//            LayBackBetOptionGroupShare groupShare = oddsShareCalculator.calculateOddsShare(layBackBetOptionGroup);
//
//            BigDecimal layUserStake = BigDecimal.ONE.divide(groupShare.getBackBetShare(), 4, RoundingMode.HALF_UP)
//                    .multiply(groupShare.getLayUserBetShare());
//
//            BigDecimal layStake = BigDecimal.ONE.divide(groupShare.getBackBetShare(), 4, RoundingMode.HALF_UP)
//                    .multiply(groupShare.getLayBetShare());
//
//            if (eventStatistic.getExpectedGoalCountStrengthBase() > points.intValue()) { //if lay won
//                return Optional.of(layUserStake.subtract(BigDecimal.ONE));
//            } else { //if back won
//                return Optional.of(BigDecimal.ONE.multiply(backBetOption.getPrice().subtract(BigDecimal.ONE)).subtract(layStake));
//            }
//        } else {//lay event not matched
//            if (eventStatistic.getExpectedGoalCountStrengthBase() > points.intValue()) {
//                return Optional.of(BigDecimal.ONE.multiply(BigDecimal.valueOf(-1)));
//            } else {
//                LayBackBetOption backBetOption = new LayBackBetOption(backBetOptionOptional.get(), BACK, backBetOptionOptional.get().getPrice()) ;
//                return Optional.of(BigDecimal.ONE.multiply(backBetOption.getPrice().subtract(BigDecimal.ONE)));
//            }
//        }
//    }
//}
