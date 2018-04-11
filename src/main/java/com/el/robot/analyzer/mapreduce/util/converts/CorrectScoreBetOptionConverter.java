package com.el.robot.analyzer.mapreduce.util.converts;

import com.el.betting.sdk.v2.Event;import com.el.betting.sdk.v4.*;
import com.el.betting.sdk.v2.OddsFormat;
import com.el.betting.sdk.v2.Period;
import com.el.betting.sdk.v2.Team;
import com.el.betting.sdk.v2.betoption.bettype.correctscore.CorrectScoreBetOption;
import com.el.betting.sdk.v2.betoption.bettype.correctscore.CorrectScoreBetOptionBuilder;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class CorrectScoreBetOptionConverter implements BetfairLineToBetOptionConverter<CorrectScoreBetOption> {

    @Override
    public CorrectScoreBetOption convertToBetOption(BetfairDataLine betfairDataLine) {
        CorrectScoreBetOptionBuilder correctScoreBetOptionBuilder = new CorrectScoreBetOptionBuilder();

        if (!betfairDataLine.getMarketName().equalsIgnoreCase("Correct Score")) {
            throw new RuntimeException("The market name is unknown: " + betfairDataLine.getMarketName());
        }

        String[] score = StringUtils.splitByWholeSeparator(betfairDataLine.getSelection(), " - ");
        Preconditions.checkArgument(score.length == 2, "Score is wrong " + betfairDataLine.getSelection());

        Event<Team> event = new Event<>();
        event.addProperty("league", betfairDataLine.getLeague());
        event.addProperty("category", betfairDataLine.getCategory());
        event.setStartTime(betfairDataLine.getEventStartTime());
        event.setParticipants(betfairDataLine.getTeams());

        correctScoreBetOptionBuilder
                .setPeriod(Period.MATCH)
                .setHomeSideScore(Integer.parseInt(score[0]))
                .setAwaySidePoints(Integer.parseInt(score[1]))
                .setPrice(betfairDataLine.getPrice())
                .setSelectionID(String.valueOf(betfairDataLine.getSelectionId()))
                .setEvent(event)
                .addAttribute("volumeMatched", betfairDataLine.getVolumeMatched())
                .setOddsFormat(OddsFormat.DECIMAL)
                .addAttribute("firstTimeMatched", betfairDataLine.getFirstTimeMatched())
                .addAttribute("lastTimeMatched", betfairDataLine.getLastTimeMatched())
                .addAttribute("marketOpenTime", betfairDataLine.getMarketOpenTime())
                .addAttribute("marketCloseTime", betfairDataLine.getMarketCloseTime())
                .setLineID(String.valueOf(betfairDataLine.getMarketId()))
                .addAttribute("betOutcome", betfairDataLine.getSelectionOutcome());

        return correctScoreBetOptionBuilder.createCorrectScoreBetOption();
    }

    @Override
    public boolean canConvertMarket(String marketName) {
        return marketName.equalsIgnoreCase("Correct Score");
    }
}
