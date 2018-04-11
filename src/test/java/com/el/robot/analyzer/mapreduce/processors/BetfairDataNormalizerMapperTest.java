package com.el.robot.analyzer.mapreduce.processors;

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

public class BetfairDataNormalizerMapperTest {

    private BetfairDataNormalizerMapper betfairDataNormalizerMapper;

    private BetfairDataNormalizerReducer betfairDataLineReducer;

    private BetOptionConverter betOptionConverter;

//    @Mock
    private WrappedMapper.Context  context;

    @Before
    public void prepare() throws IOException, InterruptedException {
        betfairDataLineReducer = new BetfairDataNormalizerReducer();
        betfairDataNormalizerMapper = new BetfairDataNormalizerMapper();
        betfairDataNormalizerMapper.setup(context);
    }

    @Test
    public void testMapCall() throws IOException, InterruptedException {
        FileUtils.readLines(new File("/var/data/PUTTABLE"))
                .stream()
                .forEach(line -> {
                    try {
                        betfairDataNormalizerMapper.map(new LongWritable(2), new Text(line), context);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }


    @Test
    public void testReducerCall() throws IOException, InterruptedException {

        Map<Text, List<Text>> listMap = new HashMap<>();
        FileUtils.readLines(new File("/var/data/PUTTABLE"))
                .stream()
                .forEach(line -> {
                    try {
                        BetfairDataLine betfairDataLine = BetfairDataReader.readLine(line);
                        Text description = new Text(betfairDataLine.getDescription());
                        if(listMap.containsKey(description)) {
                            listMap.get(description).add(new Text(line));
                        } else {
                            listMap.put(description, new ArrayList<>());
                            listMap.get(description).add(new Text(line));
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

        Map.Entry<Text, List<Text>> next = listMap.entrySet().iterator().next();
        Iterable<Text> value = next.getValue();
        betfairDataLineReducer.reduce(next.getKey(), value, null);
    }

}
