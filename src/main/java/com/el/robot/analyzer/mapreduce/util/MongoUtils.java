package com.el.robot.analyzer.mapreduce.util;

import com.el.robot.analyzer.application.HadoopRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MongoUtils {

    private final static Logger log = LoggerFactory.getLogger(MongoUtils.class);

    public static void mongoRestore(String dbName, String collection, String filePath) {
        String mongoRestoreCommand = "mongorestore --db " + dbName + " --collection " + collection + " " + filePath;
        Runtime run = Runtime.getRuntime();
        try {
            Process process = run.exec(mongoRestoreCommand);
            process.waitFor();
            BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line=buf.readLine())!=null) {
                System.out.println(line);
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
