//package com.el.robot.analyzer.simulator.service;
//
//import com.el.betting.sdk.v2.betoption.api.BetOption;
//import com.el.robot.crawler.db.v3.EventStatistic;
//import com.el.robot.bet.trading.rule.TradingRule;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//
//public interface SimulationService<T extends TradingRule, B extends BetOption> {
//
//    Optional<BigDecimal> calculatePnL(List<B> betOptionList, EventStatistic eventStatistic, T simulationRule);
//}
