package com.el.robot.analyzer.mapreduce.model;

import java.io.Serializable;

public class WritableObject implements Serializable {

    private final Object object;

    public WritableObject(Object object) {
        this.object = object;
    }

    public Object get() {
        return object;
    }
}
