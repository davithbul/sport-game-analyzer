package com.el.robot.analyzer.mapreduce.processors;

import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.processors.tick.TickProgressMapper;
import com.el.robot.analyzer.mapreduce.util.BetOptionConverter;
import com.el.robot.analyzer.mapreduce.util.BetfairDataLine;
import com.el.robot.analyzer.mapreduce.util.BetfairDataReader;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.map.WrappedMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TickProgressMapperTest {

    private TickProgressMapper tickProgressMapper;

    private BetOptionConverter betOptionConverter;

//    @Mock
    private WrappedMapper.Context  context;

    @Before
    public void prepare() throws IOException, InterruptedException {
        tickProgressMapper = new TickProgressMapper();
        tickProgressMapper.setup(context);
    }

    @Test
    public void testMapCall() throws IOException, InterruptedException {
        Text[] values = FileUtils.readLines(new File("/var/data/test2.csv"))
                .stream()
                .map(Text::new)
                .toArray(Text[]::new);

        TextArrayWritable textArrayWritable = new TextArrayWritable(values);

        tickProgressMapper.map(null, textArrayWritable, context);
    }
}
