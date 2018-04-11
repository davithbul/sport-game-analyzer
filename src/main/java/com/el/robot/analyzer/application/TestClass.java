package com.el.robot.analyzer.application;

import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.processors.BetOptionStatsMapper;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.Text;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class TestClass {

    public static void main(String[] args) throws IOException, InterruptedException {
        Text[] values = FileUtils.readLines(new File("/var/data/correct_scores"))
                .stream()
                .map(Text::new)
                .toArray(Text[]::new);

        BetOptionStatsMapper betOptionStatsMapper = new BetOptionStatsMapper();
        TextArrayWritable textArrayWritable = new TextArrayWritable(values);
        betOptionStatsMapper.map(null, textArrayWritable, null);
    }
}
