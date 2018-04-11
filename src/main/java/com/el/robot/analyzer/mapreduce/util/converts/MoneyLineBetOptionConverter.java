package com.el.robot.analyzer.mapreduce.util.converts;

import com.el.betting.sdk.v2.OddsFormat;
import com.el.betting.sdk.v2.Period;
import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.betoption.bettype.moneyline.MoneyLineBetOption;
import com.el.betting.sdk.v2.betoption.bettype.moneyline.MoneyLineBetOptionBuilder;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;

public class MoneyLineBetOptionConverter implements BetfairLineToBetOptionConverter<MoneyLineBetOption> {

    @Override
    public MoneyLineBetOption convertToBetOption(BetfairDataLine betfairDataLine) {
        MoneyLineBetOptionBuilder moneyLineBetOptionBuilder = new MoneyLineBetOptionBuilder();

        if (betfairDataLine.getMarketName().equalsIgnoreCase("Match Odds")) {
            moneyLineBetOptionBuilder.setPeriod(Period.MATCH);
        } else if (betfairDataLine.getMarketName().equalsIgnoreCase("Half Time")) {
            moneyLineBetOptionBuilder.setPeriod(Period.FIRST_HALF);
        } else {
            throw new RuntimeException("Unknown market nam e " + betfairDataLine.getMarketName());
        }

        if (betfairDataLine.getSelection().equalsIgnoreCase("The draw")) {
            moneyLineBetOptionBuilder.setTeam(Team.DRAW);
        } else if (betfairDataLine.getSelection().equalsIgnoreCase(betfairDataLine.getHomeTeam().getName())) {
            moneyLineBetOptionBuilder.setTeam(betfairDataLine.getHomeTeam());
        } else if (betfairDataLine.getSelection().equalsIgnoreCase(betfairDataLine.getAwayTeam().getName())) {
            moneyLineBetOptionBuilder.setTeam(betfairDataLine.getAwayTeam());
        } else {
            throw new RuntimeException("Team name not found in competing team list: " + betfairDataLine.getSelection());
        }

        moneyLineBetOptionBuilder
                .setPrice(betfairDataLine.getPrice())
                .setSelectionID(String.valueOf(betfairDataLine.getSelectionId()))
                .addAttribute("volumeMatched", betfairDataLine.getVolumeMatched())
                .setOddsFormat(OddsFormat.DECIMAL)
                .addAttribute("firstTimeMatched", betfairDataLine.getFirstTimeMatched())
                .addAttribute("lastTimeMatched", betfairDataLine.getLastTimeMatched())
                .setLineID(String.valueOf(betfairDataLine.getMarketId()))
                .addAttribute("betOutcome", betfairDataLine.getSelectionOutcome());

        return moneyLineBetOptionBuilder.createMoneyLineBetOption();
    }

    @Override
    public boolean canConvertMarket(String marketName) {
        return marketName.equalsIgnoreCase("Match Odds") || marketName.equalsIgnoreCase("Half Time");
    }
}
