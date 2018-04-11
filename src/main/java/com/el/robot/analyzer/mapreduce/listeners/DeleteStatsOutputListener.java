package com.el.robot.analyzer.mapreduce.listeners;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DeleteStatsOutputListener implements StepExecutionListener {

    private final static Logger log = LoggerFactory.getLogger(DeleteAggregationOutputListener.class);

    private String statisticOutputPath;

    @Autowired
    public DeleteStatsOutputListener(@Value("${betfair.stats.data.path}") String statisticOutputPath) {
        this.statisticOutputPath = statisticOutputPath;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            String date = stepExecution.getJobParameters().getString("date");
            String outputPath = statisticOutputPath + "/" + date;
            FileSystem fileSystem = FileSystem.get(new Configuration());
            log.debug("Deleting aggregation output path: {}", outputPath);
            fileSystem.delete(new Path(outputPath), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return stepExecution.getExitStatus();
    }
}
