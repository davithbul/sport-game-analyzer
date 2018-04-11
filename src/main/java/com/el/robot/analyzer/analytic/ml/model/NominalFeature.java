package com.el.robot.analyzer.analytic.ml.model;

import java.util.Arrays;
import java.util.List;

/**
 * Represents feature which has fixed value set.
 * I.e. Boolean has {true, false} nominals.
 */
public class NominalFeature extends Feature {

    private final List<String> nominals;

    public NominalFeature(String name, String... nominals) {
        this(name, Arrays.asList(nominals));
    }

    public NominalFeature(String name, List<String> nominals) {
        super(name);
        this.nominals = nominals;
    }

    public List<String> getNominals() {
        return nominals;
    }

    @Override
    public String toString() {
        return "NominalFeature{" +
                "nominals=" + nominals +
                "} " + super.toString();
    }
}
