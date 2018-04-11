package com.el.robot.analyzer.analytic.ml.model;

import java.util.Arrays;

public class BooleanFeature extends NominalFeature {

    public BooleanFeature(String name) {
        super(name, Arrays.asList("true", "false"));
    }
}
