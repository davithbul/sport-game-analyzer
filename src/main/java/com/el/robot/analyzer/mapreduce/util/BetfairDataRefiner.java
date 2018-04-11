package com.el.robot.analyzer.mapreduce.util;

import org.apache.commons.lang3.StringUtils;

public class BetfairDataRefiner {

    public static void fillBetfairLine(BetfairDataLine betfairDataLine, BetfairDataLine correctBetfairLine) {
        if(betfairDataLine.getSportId() <= 0 && correctBetfairLine.getSportId() > 0) {
            betfairDataLine.setSportId(correctBetfairLine.getSportId());
        }

        if(betfairDataLine.getMarketId() == null && correctBetfairLine.getMarketId() > 0) {
            betfairDataLine.setMarketId(correctBetfairLine.getMarketId());
        }

        if(StringUtils.isBlank(betfairDataLine.getMarketName()) && !StringUtils.isBlank(correctBetfairLine.getMarketName())) {
            betfairDataLine.setMarketName(correctBetfairLine.getMarketName());
        }

        if(betfairDataLine.getMarketCloseTime() == null && correctBetfairLine.getMarketCloseTime() != null) {
            betfairDataLine.setMarketCloseTime(correctBetfairLine.getMarketCloseTime());
        }

        if(betfairDataLine.getMarketOpenTime() == null && correctBetfairLine.getMarketOpenTime() != null) {
            betfairDataLine.setMarketOpenTime(correctBetfairLine.getMarketOpenTime());
        }

        if(betfairDataLine.getEventStartTime() == null && correctBetfairLine.getEventStartTime() != null) {
            betfairDataLine.setEventStartTime(correctBetfairLine.getEventStartTime());
        }
    }
}
