package com.el.robot.analyzer.mapreduce.batch.steps;

import com.el.robot.analyzer.mapreduce.util.MongoUtils;
import com.el.robot.crawler.db.v3.EventStatisticManager;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.RemoteIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

@Component
public class LoadStatsToMongo implements Tasklet {

    private final static Logger log = LoggerFactory.getLogger(LoadStatsToMongo.class);

    private final static String MONGO_BSON_OUTPUT_DIR = "/var/data/mongo/output";

    private final static String DB_NAME = "wise_robots";

    @Autowired
    private EventStatisticManager eventStatisticManager;

    @Value("${betfair.stats.data.path}")
    private String HDFS_STATS_PATH;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        String date = (String) chunkContext.getStepContext().getJobParameters().get("date");
        String mongoLocalOutputDir = MONGO_BSON_OUTPUT_DIR + "/" + date;
        Path mongoOutputPath = Paths.get(mongoLocalOutputDir);
        org.apache.hadoop.fs.Path hdfsMongoOutputDir = new org.apache.hadoop.fs.Path(HDFS_STATS_PATH, date);

        //clean old bson files
        if(!mongoOutputPath.toFile().exists()) {
            Files.createDirectory(mongoOutputPath);
        }
        FileUtils.cleanDirectory(mongoOutputPath.toFile());

        //copy mongo bson files from hdfs to local environment
        FileSystem fileSystem = FileSystem.get(new Configuration());
        RemoteIterator<LocatedFileStatus> bsonFiles = fileSystem.listFiles(hdfsMongoOutputDir, false);
        while (bsonFiles.hasNext()) {
            LocatedFileStatus bsonFile = bsonFiles.next();
            if(bsonFile.getPath().getName().endsWith(".bson")) {
                fileSystem.copyToLocalFile(false, bsonFile.getPath(), new org.apache.hadoop.fs.Path(mongoOutputPath.toAbsolutePath().toString()));
            }
        }

        //delete from mongo db old entries
        LocalDate localDate = LocalDate.parse(date, BASIC_ISO_DATE);
        LocalDateTime startDate = localDate.atStartOfDay();
        LocalDateTime endDate = startDate.plusDays(6)
                .withHour(23).withMinute(59).withSecond(59);
        log.debug("Cleaning from mongo db old statistics from {} to {}.", startDate, endDate);
        Query query = new Query();
        query.addCriteria(Criteria.where("startTime").gte(startDate.minusHours(2)).lte(endDate.minusHours(2)));
        eventStatisticManager.delete(query);

        //now load stats from bson to mongo db
        Files.newDirectoryStream(mongoOutputPath).forEach(file -> {
            MongoUtils.mongoRestore(DB_NAME, eventStatisticManager.getCollectionName(), file.toAbsolutePath().toString());
        });

        return RepeatStatus.FINISHED;
    }
}
