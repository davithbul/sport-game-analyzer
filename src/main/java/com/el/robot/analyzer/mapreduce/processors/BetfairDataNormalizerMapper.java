package com.el.robot.analyzer.mapreduce.processors;

import com.el.robot.analyzer.mapreduce.util.BetOptionConverter;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;
import com.el.robot.analyzer.mapreduce.util.BetfairDataReader;
import com.el.robot.analyzer.mapreduce.util.converts.CorrectScoreBetOptionConverter;
import com.el.robot.analyzer.mapreduce.util.converts.MoneyLineBetOptionConverter;
import com.el.robot.analyzer.mapreduce.util.converts.TotalPointsBetOptionConverter;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.time.format.DateTimeParseException;


@ThreadSafe
public class BetfairDataNormalizerMapper extends Mapper<LongWritable, Text, Text, Text> {

    private final static Logger log = LoggerFactory.getLogger(BetfairDataNormalizerMapper.class);

    private BetOptionConverter betOptionConverter;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        betOptionConverter = new BetOptionConverter(
                new CorrectScoreBetOptionConverter(),
                new MoneyLineBetOptionConverter(),
                new TotalPointsBetOptionConverter());
        super.setup(context);
    }

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        //SPORTS_ID,EVENT_ID,SETTLED_DATE,FULL_DESCRIPTION,SCHEDULED_OFF,EVENT,DT ACTUAL_OFF,SELECTION_ID,SELECTION,ODDS,NUMBER_BETS,VOLUME_MATCHED,LATEST_TAKEN,FIRST_TAKEN,WIN_FLAG,IN_PLAY
        //1,123447909,13-03-2016 19:59:23,Spanish Soccer/Primera Division/Fixtures 13 March    /Las Palmas v Real Madrid,13-03-2016 19:30,First Half Goals 0.5,13-03-2016 19:34:03,5851482,Under 0.5 Goals,3.5,14,375.96,13-03-2016 19:47:50,13-03-2016 19:46:33,0,IP

        if (key.get() < 1) { //ignore header
            return;
        }

        BetfairDataLine betfairDataLine;
        try {
            betfairDataLine = BetfairDataReader.readLine(value.toString());
        } catch (DateTimeParseException | IllegalArgumentException e) {
            log.error(e.getMessage(), e);
            return;
        }

        if (betfairDataLine.getTeams() == null || betfairDataLine.getTeams().size() != 2) {
            return;
        }

        if (betOptionConverter.hasMatchingConverter(betfairDataLine)) {
            context.write(new Text(betfairDataLine.getDescription()), value);
        }
    }
}
