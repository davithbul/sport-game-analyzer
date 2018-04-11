package com.el.robot.analyzer.analytic.ml.services;

import com.el.robot.analyzer.analytic.ml.exceptions.ClassificationFailedException;
import weka.classifiers.Classifier;
import weka.core.Attribute;

import java.util.ArrayList;
import java.util.Map;

/**
 * {@link MLPredictionService} is a center of machine learning prediction framework.
 * It provides accessible functions for predicted value for a new unclassified data.
 */
public interface MLPredictionService {
    double predictNumericValue(Classifier classifier, ArrayList<Attribute> attributes, Object... instanceValues) throws ClassificationFailedException;

    String predictNominalValue(Classifier classifier, ArrayList<Attribute> attributes, Object... instanceValues) throws ClassificationFailedException;

    /**
     * Returns the probability of distribution order by highest probability.
     * The key of the map is a name of nominal, the value is a probability.
     */
    Map<String, Double> measureDistribution(Classifier classifier, ArrayList<Attribute> attributes, Object... instanceValues) throws ClassificationFailedException;
}
