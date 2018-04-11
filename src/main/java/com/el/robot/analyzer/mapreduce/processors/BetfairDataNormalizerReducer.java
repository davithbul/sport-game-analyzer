package com.el.robot.analyzer.mapreduce.processors;

import com.el.betting.sdk.v2.BetType;
import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;
import com.el.robot.analyzer.mapreduce.util.BetfairDataReader;
import com.el.robot.analyzer.mapreduce.util.BetfairDataRefiner;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ThreadSafe
public class BetfairDataNormalizerReducer extends Reducer<Text, Text, Text, TextArrayWritable> {

    private final static Logger log = LoggerFactory.getLogger(BetfairDataNormalizerReducer.class);

    @Override
    protected void reduce(Text eventDetails, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        List<BetfairDataLine> betfairDataLines = new ArrayList<>();

        for (Text value : values) {
            BetfairDataLine betfairDataLine = BetfairDataReader.readLine(value.toString());
            betfairDataLines.add(betfairDataLine);
        }

        //fill correctBetfairLine with correct values
        BetfairDataLine correctBetfairLine = new BetfairDataLine();
        for (BetfairDataLine betfairDataLine : betfairDataLines) {
            BetfairDataRefiner.fillBetfairLine(correctBetfairLine, betfairDataLine);
        }

        //now fill all wrong betfair liens with correct values
        for (BetfairDataLine betfairDataLine : betfairDataLines) {
            BetfairDataRefiner.fillBetfairLine(betfairDataLine, correctBetfairLine);
        }

        Text[] texts = new Text[betfairDataLines.size()];
        for (int i = 0; i < betfairDataLines.size(); i++) {
            texts[i] = new Text(BetfairDataReader.toString(betfairDataLines.get(i)));
        }

        TextArrayWritable arrayWritable = new TextArrayWritable(texts);
        context.write(eventDetails, arrayWritable);
        context.getCounter(BetType.CORRECT_SCORE).increment(1);
    }
}
