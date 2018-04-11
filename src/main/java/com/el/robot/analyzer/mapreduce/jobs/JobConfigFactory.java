package com.el.robot.analyzer.mapreduce.jobs;

import com.el.robot.analyzer.mapreduce.model.TextArrayWritable;
import com.el.robot.analyzer.mapreduce.processors.BetOptionStatsMapper;
import com.el.robot.analyzer.mapreduce.processors.BetfairDataNormalizerMapper;
import com.el.robot.analyzer.mapreduce.processors.BetfairDataNormalizerReducer;
import com.el.robot.analyzer.mapreduce.processors.BetOptionSimulatorMapper;
import com.mongodb.hadoop.BSONFileOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;

import java.io.IOException;

public class JobConfigFactory {

    public static Job getBetOptionNormalizerJob() throws IOException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Bet Option Normalizer");
        job.setMapperClass(BetfairDataNormalizerMapper.class);
//        job.setCombinerClass(SimpleReducer.class);
        job.setReducerClass(BetfairDataNormalizerReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(TextArrayWritable.class);
        job.setOutputFormatClass(SequenceFileOutputFormat.class);
        return job;
    }

    public static Job getLoadStatsToMongoJob() throws IOException {
        Configuration conf = new Configuration();
//        conf.set("io.serializations",
//                "org.apache.hadoop.io.serializer.JavaSerialization," +
//                        "org.apache.hadoop.io.serializer.WritableSerialization");


        Job job = Job.getInstance(conf, "Get Load Stats to Mongo Job");
        job.setInputFormatClass(SequenceFileInputFormat.class);
//        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setMapperClass(BetOptionStatsMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(BSONFileOutputFormat.class);
        job.setNumReduceTasks(0);
        return job;
    }

    public static Job getSimulatorJob() throws IOException {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Simulator Job");
        job.setInputFormatClass(SequenceFileInputFormat.class);
//        job.setInputFormatClass(KeyValueTextInputFormat.class);
        job.setMapperClass(BetOptionSimulatorMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setNumReduceTasks(0);
        return job;
    }
}
