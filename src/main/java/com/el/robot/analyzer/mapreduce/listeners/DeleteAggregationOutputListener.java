package com.el.robot.analyzer.mapreduce.listeners;

import com.el.robot.analyzer.mapreduce.processors.BetOptionStatsMapper;
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
public class DeleteAggregationOutputListener implements StepExecutionListener {

    private final static Logger log = LoggerFactory.getLogger(DeleteAggregationOutputListener.class);

    private String statsOutputPath;

    @Autowired
    public DeleteAggregationOutputListener(@Value("${betfair.aggregated.data.path}") String statsOutputPath) {
        this.statsOutputPath = statsOutputPath;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        try {
            String date = stepExecution.getJobParameters().getString("date");
            String outputPath = statsOutputPath + "/" + date;
            FileSystem fileSystem = FileSystem.get(new Configuration());
            log.debug("Deleting Statistic output path: {}", outputPath);
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
