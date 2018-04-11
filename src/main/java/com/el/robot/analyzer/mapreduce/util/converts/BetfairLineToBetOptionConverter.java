package com.el.robot.analyzer.mapreduce.util.converts;

import com.el.betting.sdk.v2.betoption.api.BetOption;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;

public interface BetfairLineToBetOptionConverter<T extends BetOption> {

    T convertToBetOption(BetfairDataLine line);

    boolean canConvertMarket(String marketName);
}
