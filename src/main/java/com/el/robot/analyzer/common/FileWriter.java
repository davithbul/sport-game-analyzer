package com.el.robot.analyzer.common;

import java.io.IOException;

@FunctionalInterface
public interface FileWriter {
    void writeLine(String line) throws IOException;
}
