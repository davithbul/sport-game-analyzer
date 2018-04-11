package com.el.robot.analyzer.mapreduce.jobs;

import com.el.robot.analyzer.application.HadoopRunner;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class JobExecutor {
    public boolean normalizeBetOptions(String inputPath, String outputPath) throws IOException, ClassNotFoundException, InterruptedException {
        FileSystem fileSystem = FileSystem.get(new Configuration());
        fileSystem.delete(new Path(outputPath), true);
        Job job = JobConfigFactory.getBetOptionNormalizerJob();
        job.setJarByClass(HadoopRunner.class);
        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        return job.waitForCompletion(true);
    }

    public boolean loadBetOptionStatistics(String inputPath, String outputPath) throws InterruptedException, IOException, ClassNotFoundException {
        FileSystem fileSystem = FileSystem.get(new Configuration());
        fileSystem.delete(new Path(outputPath), true);
        Job loadStatsToMongoJob = JobConfigFactory.getLoadStatsToMongoJob();
        loadStatsToMongoJob.setJarByClass(HadoopRunner.class);
        FileInputFormat.addInputPath(loadStatsToMongoJob, new Path(inputPath));
        FileOutputFormat.setOutputPath(loadStatsToMongoJob, new Path(outputPath));
        return loadStatsToMongoJob.waitForCompletion(true);
    }

    public boolean simulate(String inputPath, String outputPath) throws InterruptedException, IOException, ClassNotFoundException {
        FileSystem fileSystem = FileSystem.get(new Configuration());
        fileSystem.delete(new Path(outputPath), true);
        Job loadStatsToMongoJob = JobConfigFactory.getSimulatorJob();
        loadStatsToMongoJob.setJarByClass(HadoopRunner.class);
        FileInputFormat.addInputPath(loadStatsToMongoJob, new Path(inputPath));
        FileOutputFormat.setOutputPath(loadStatsToMongoJob, new Path(outputPath));
        return loadStatsToMongoJob.waitForCompletion(true);
    }
}
