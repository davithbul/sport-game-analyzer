package com.el.robot.analyzer.common;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class CsvFileWriter implements FileWriter {

    private final static String DEFAULT_SEPARATOR = ",";
    private final String separator;
    private final Path path;
    private transient boolean firstLine = true;

    public CsvFileWriter(String fileName) throws IOException {
        this(fileName, DEFAULT_SEPARATOR);
    }

    public CsvFileWriter(String fileName, String separator) throws IOException {
        this.path = Paths.get(fileName);
        this.separator = separator;
    }

    @Override
    public void writeLine(String line) throws IOException {
        if(firstLine) {
            firstLine = false;
            Files.write(path, (line + "\n").getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            Files.write(path, (line + "\n").getBytes(), StandardOpenOption.APPEND);
        }
    }

    public void writeLine(List<? extends Object> csvValues) throws IOException {
        writeLine(StringUtils.join(csvValues, separator));
    }

    public void writeLine(Object... csvValues) throws IOException {
        writeLine(StringUtils.join(csvValues, separator));
    }
}
