package com.el.robot.analyzer.mapreduce.util.converts;

import com.el.betting.common.RegexpUtil;
import com.el.betting.sdk.v2.OddsFormat;
import com.el.betting.sdk.v2.Period;
import com.el.betting.sdk.v2.TotalType;
import com.el.betting.sdk.v2.betoption.bettype.totalpoints.TotalPointsBetOption;
import com.el.betting.sdk.v2.betoption.bettype.totalpoints.TotalPointsBetOptionBuilder;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;

import java.math.BigDecimal;
import java.util.Optional;

public class TotalPointsBetOptionConverter implements BetfairLineToBetOptionConverter<TotalPointsBetOption> {

    @Override
    public TotalPointsBetOption convertToBetOption(BetfairDataLine betfairDataLine) {
        TotalPointsBetOptionBuilder totalPointsBetOptionBuilder = new TotalPointsBetOptionBuilder();
        double points;
        if (betfairDataLine.getMarketName().matches("Over/Under ([0-9]*\\.?[0-9]+) Goals")) {
            points = Double.valueOf(RegexpUtil.getRegexpGroup(betfairDataLine.getMarketName(), "Over/Under ([0-9]*\\.?[0-9]+) Goals", 1).get());
            totalPointsBetOptionBuilder.setPeriod(Period.MATCH)
                    .setPoints(points);
        } else if (betfairDataLine.getMarketName().matches("First Half Goals ([0-9]*\\.?[0-9]+)")) {
            points = Double.valueOf(RegexpUtil.getRegexpGroup(betfairDataLine.getMarketName(), "First Half Goals ([0-9]*\\.?[0-9]+)", 1).get());
            totalPointsBetOptionBuilder.setPeriod(Period.FIRST_HALF)
                    .setPoints(points);
        } else {
            throw new RuntimeException("Unknow market name: " + betfairDataLine.getMarketName());
        }


        Optional<String> totalTypeOptional = RegexpUtil.getRegexpGroup(betfairDataLine.getSelection(), "(Under|Over) " + points + " Goals", 1);
        TotalType totalType = TotalType.valueOf(totalTypeOptional.get().toUpperCase());

        totalPointsBetOptionBuilder
                .setTotalType(totalType)
                .setPrice(betfairDataLine.getPrice())
                .setSelectionID(String.valueOf(betfairDataLine.getSelectionId()))
                .addAttribute("volumeMatched", betfairDataLine.getVolumeMatched())
                .setOddsFormat(OddsFormat.DECIMAL)
                .addAttribute("firstTimeMatched", betfairDataLine.getFirstTimeMatched())
                .addAttribute("lastTimeMatched", betfairDataLine.getLastTimeMatched())
                .setLineID(String.valueOf(betfairDataLine.getMarketId()))
                .addAttribute("betOutcome", betfairDataLine.getSelectionOutcome())
                .addAttribute("marketOpenTime", betfairDataLine.getMarketOpenTime())
                .createTotalPointsBetOption();

        return totalPointsBetOptionBuilder.createTotalPointsBetOption();
    }

    @Override
    public boolean canConvertMarket(String marketName) {
        return marketName.matches("Over/Under ([0-9]*\\.?[0-9]+) Goals") ||
                marketName.matches("First Half Goals ([0-9]*\\.?[0-9]+)");
    }
}
