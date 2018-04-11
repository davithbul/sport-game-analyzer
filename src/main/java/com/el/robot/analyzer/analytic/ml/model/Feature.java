package com.el.robot.analyzer.analytic.ml.model;

public class Feature {
    /** name of the feature **/
    private final String name;

    public Feature(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    @Override
    public String toString() {
        return "Feature{" +
                "name='" + name + '\'' +
                '}';
    }
}
