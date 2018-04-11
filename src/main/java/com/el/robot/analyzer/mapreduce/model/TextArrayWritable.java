package com.el.robot.analyzer.mapreduce.model;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;

public class TextArrayWritable extends ArrayWritable {

    public TextArrayWritable() {
        super(Text.class);
    }

    public TextArrayWritable(Text[] values) {
        super(Text.class, values);
    }
}
