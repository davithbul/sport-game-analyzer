package com.el.robot.analyzer.mapreduce.processors;

import com.el.betting.sdk.v2.BetType;
import com.el.betting.sdk.v2.betoption.bettype.totalpoints.TotalPointsBetOption;
import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;
import com.el.robot.analyzer.mapreduce.util.BetfairDataReader;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class BetOptionSimulatorMapper extends Mapper<Object, TextArrayWritable, Text, Text> {

    @Override
    protected void map(Object key, TextArrayWritable values, Context context) throws IOException, InterruptedException {
        for (Writable writable : values.get()) {
            Text text = (Text) writable;
            BetfairDataLine betfairDataLine = BetfairDataReader.readLine(text.toString());
            TotalPointsBetOption totalPointsBetOption;
        }
    }
}
