package com.el.robot.analyzer.mapreduce.listeners;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteIntermediateFiles implements StepExecutionListener {

    @Autowired
    private DeleteAggregationOutputListener deleteAggregationOutputListener;

    @Autowired
    private DeleteStatsOutputListener deleteStatsOutputListener;

    @Override
    public void beforeStep(StepExecution stepExecution) {
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        deleteAggregationOutputListener.beforeStep(stepExecution);
        deleteStatsOutputListener.beforeStep(stepExecution);
        return stepExecution.getExitStatus();
    }
}
