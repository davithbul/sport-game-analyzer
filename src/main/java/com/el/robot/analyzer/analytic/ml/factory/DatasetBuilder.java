package com.el.robot.analyzer.analytic.ml.factory;

import com.el.robot.analyzer.analytic.ml.model.Feature;

import java.util.List;

/**
 * Factory class for building classifiers.
 */
public abstract class DatasetBuilder {

    /**
     * Builds dataset for given features.
     * @param name the name of the dataset
     * @param features the list of features which data set will have - in other words it's header of dataset
     * @param attributeValues the list of attribute values, each list represents one combination of values
     * @return returns prepared dataset
     */
    public abstract List<?> buildDataSet(String name, Feature[] features, Object[]... attributeValues);
}
