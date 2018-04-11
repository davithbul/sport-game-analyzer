package com.el.robot.analyzer.mapreduce.util;

import com.el.betting.common.TeamUtils;
import com.el.betting.sdk.v2.BetOutcome;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BetfairDataReader {

    private static final DateTimeFormatter DATE_TIME_SECOND_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public static BetfairDataLine readLine(String line) {
        String[] splits = StringUtils.splitByWholeSeparatorPreserveAllTokens(line.substring(1, line.length() - 1), "\",\"");

        BetfairDataLine betfairDataLine = new BetfairDataLine();
        betfairDataLine.setSportId(Long.parseLong(splits[0]));
        betfairDataLine.setMarketName(splits[5]);

        if (betfairDataLine.getSportId() != 1 || betfairDataLine.getMarketName().isEmpty()) { //if market is empty
            return betfairDataLine;
        }

        betfairDataLine.setMarketId(!splits[1].isEmpty() ? Long.parseLong(splits[1]) : null);
        betfairDataLine.setMarketCloseTime(!splits[2].isEmpty() ? LocalDateTime.parse(splits[2], DATE_TIME_SECOND_FORMATTER) : null);

        String description = splits[3];
        betfairDataLine.setDescription(description);
        String[] descriptionSplits = StringUtils.split(description, "/");
        if (descriptionSplits.length >= 3) {
            betfairDataLine.setCategory(descriptionSplits.length == 4 ? descriptionSplits[0].trim() : null);
            betfairDataLine.setLeague(descriptionSplits[descriptionSplits.length - 3].trim());
            betfairDataLine.setDetails(descriptionSplits[descriptionSplits.length - 2].trim());
            betfairDataLine.setTeams(TeamUtils.getTeams(descriptionSplits[descriptionSplits.length - 1], " v "));
        }
        betfairDataLine.setEventStartTime(LocalDateTime.parse(splits[4], DATE_TIME_FORMATTER));
        betfairDataLine.setMarketOpenTime(!splits[6].isEmpty() ? LocalDateTime.parse(splits[6], DATE_TIME_SECOND_FORMATTER) : null);
        betfairDataLine.setSelectionId(Long.parseLong(splits[7]));
        betfairDataLine.setSelection(splits[8]);
        betfairDataLine.setPrice(new BigDecimal(splits[9]));
        betfairDataLine.setNumberOfBets(Long.parseLong(splits[10]));
        betfairDataLine.setVolumeMatched(new BigDecimal(splits[11]));
        betfairDataLine.setLastTimeMatched(LocalDateTime.parse(splits[12], DATE_TIME_SECOND_FORMATTER));
        betfairDataLine.setFirstTimeMatched(LocalDateTime.parse(splits[13], DATE_TIME_SECOND_FORMATTER));
        betfairDataLine.setSelectionOutcome(Integer.valueOf(splits[14]) == 1 ? BetOutcome.WON : BetOutcome.LOST);
        boolean inPlay = splits[15].equals("IP");
        return betfairDataLine;
    }

    public static String toString(BetfairDataLine betfairDataLine) {
        return "\""
                + betfairDataLine.getSportId() + "\",\""
                + betfairDataLine.getMarketId() + "\",\""
                + betfairDataLine.getMarketCloseTime().format(DATE_TIME_SECOND_FORMATTER) + "\",\""
                + betfairDataLine.getDescription() + "\",\""
                + betfairDataLine.getEventStartTime().format(DATE_TIME_FORMATTER) + "\",\""
                + betfairDataLine.getMarketName() + "\",\""
                + (betfairDataLine.getMarketOpenTime() != null ? betfairDataLine.getMarketOpenTime().format(DATE_TIME_SECOND_FORMATTER) : "") + "\",\""
                + betfairDataLine.getSelectionId() + "\",\""
                + betfairDataLine.getSelection() + "\",\""
                + betfairDataLine.getPrice() + "\",\""
                + betfairDataLine.getNumberOfBets() + "\",\""
                + betfairDataLine.getVolumeMatched() + "\",\""
                + betfairDataLine.getLastTimeMatched().format(DATE_TIME_SECOND_FORMATTER) + "\",\""
                + betfairDataLine.getFirstTimeMatched().format(DATE_TIME_SECOND_FORMATTER) + "\",\""
                + (betfairDataLine.getSelectionOutcome() == BetOutcome.WON ? 1 : 0) + "\",\""
                + "IP\"";
    }
}
