
package com.el.robot.analyzer.mapreduce.processors;

import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.util.BetOptionConverter;
import com.el.robot.analyzer.mapreduce.util.converts.CorrectScoreBetOptionConverter;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.map.WrappedMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class BetOptionStatsMapperTest {

    private BetOptionStatsMapper betOptionStatsMapper;

    private BetOptionConverter betOptionConverter;

    @Mock
    private MongoConverter mongoConverter;

    @Mock
    private WrappedMapper.Context  context;

    @Before
    public void prepare() {
        betOptionStatsMapper = new BetOptionStatsMapper();
        betOptionConverter = new BetOptionConverter(new CorrectScoreBetOptionConverter());
        ReflectionTestUtils.setField(betOptionStatsMapper, "betOptionConverter", betOptionConverter);
        ReflectionTestUtils.setField(betOptionStatsMapper, "mongoConverter", mongoConverter);
    }

    @Test
    public void testMapCall() throws IOException, InterruptedException {
        Text[] values = FileUtils.readLines(new File("/var/data/bayern_vs_werder_correct_score.csv"))
                .stream()
                .map(Text::new)
                .toArray(Text[]::new);

        TextArrayWritable textArrayWritable = new TextArrayWritable(values);

        betOptionStatsMapper.map(null, textArrayWritable, context);
    }

}

