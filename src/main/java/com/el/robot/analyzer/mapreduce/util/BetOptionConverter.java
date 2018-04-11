package com.el.robot.analyzer.mapreduce.util;

import com.el.betting.sdk.v2.betoption.api.BetOption;
import com.el.robot.analyzer.mapreduce.util.converts.BetfairLineToBetOptionConverter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BetOptionConverter {
    private List<BetfairLineToBetOptionConverter> betOptionConverters;

    public BetOptionConverter(BetfairLineToBetOptionConverter... betOptionConverters) {
        this.betOptionConverters = Arrays.asList((BetfairLineToBetOptionConverter[]) betOptionConverters);
    }

    public Optional<BetOption> convertToBetOption(BetfairDataLine betfairDataLine) {
        return betOptionConverters.stream()
                .filter(betfairLineToBetOptionConverter -> betfairLineToBetOptionConverter.canConvertMarket(betfairDataLine.getMarketName()))
                .map(betfairLineToBetOptionConverter -> betfairLineToBetOptionConverter.convertToBetOption(betfairDataLine))
                .findFirst();
    }

    public boolean hasMatchingConverter(BetfairDataLine betfairDataLine) {
        return betOptionConverters.stream()
                .anyMatch(betfairLineToBetOptionConverter -> betfairLineToBetOptionConverter.canConvertMarket(betfairDataLine.getMarketName()));
    }
}
