package com.el.robot.analyzer.application;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class HadoopRunner {

    private final static Logger log = LoggerFactory.getLogger(HadoopRunner.class);

    public static void main(String[] args) throws IOException {
        String date = args[0];
        Preconditions.checkArgument(Integer.parseInt(date) > 2016);
        System.setProperty("betfair.data.date", date);

        AbstractApplicationContext context = new ClassPathXmlApplicationContext(
                "/application-context.xml", HadoopRunner.class);

        log.info("Hadoop runner starting...");
        log.info("Date is " + date);
        context.registerShutdownHook();

        JobLauncher jobLauncher = (JobLauncher) context.getBean("jobLauncher");
        Job job = (Job) context.getBean("loadStatsBatchJob");
        JobOperator jobOperator = (JobOperator) context.getBean("jobOperator");

        if(args.length > 1 && args[1].equalsIgnoreCase("RERUN")) {
            try {
                Long exitValue = jobOperator.startNextInstance(job.getName());
                System.out.println("Exit Status : " + exitValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                JobExecution execution = jobLauncher.run(job, new JobParameters(
                        ImmutableMap.of("date", new JobParameter(String.valueOf(date))))
                );
                System.out.println("Exit Status : " + execution.getStatus());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Done");
    }
}
