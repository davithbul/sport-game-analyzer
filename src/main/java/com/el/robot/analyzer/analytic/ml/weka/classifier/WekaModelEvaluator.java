package com.el.robot.analyzer.analytic.ml.weka.classifier;

import com.el.robot.analyzer.analytic.ml.exceptions.MLEvaluationException;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Debug;
import weka.core.Instances;

public class WekaModelEvaluator {

    /**
     * Gets the number of instances correctly classified (that is, for which a
     * correct prediction was made). (Actually the sum of the weights of these
     * instances)
     *
     * @return percentage of correctly classified instances
     */
    public double evaluateModel(Classifier classifier, Instances trainInstances, Instances testInstances) throws MLEvaluationException {
        try {
            Evaluation evaluation = new Evaluation(trainInstances);
            double[] doubles = evaluation.evaluateModel(classifier, testInstances);
            return evaluation.pctCorrect();
        } catch (Exception e) {
            throw new MLEvaluationException(e);
        }
    }

    /**
     * Validates model by separating data into folds parts and training data with the first part, then
     * testing the result with other part of data.
     * E.G.
     * if numFolder = 10 and we have 200 training samples
     * then it will separate 10 different folds,
     * In each fold it will have 180 training data and 20 testing data
     * For each fold it will evaluate model for 20 testing data using training data.
     * The mean of the all evaluated models will be the final evaluation.
     * @implNote {@link weka.core.Debug.Random} will be used for randomly separating training and testing data sets.
     *
     */
    public double crossEvaluateModel(Classifier classifier, Instances instances, int numFolds) throws MLEvaluationException {
        try {
            Evaluation evaluation = new Evaluation(instances);
            evaluation.crossValidateModel(classifier, instances, numFolds, new Debug.Random(1));
            return evaluation.pctCorrect();
        } catch (Exception e) {
            throw new MLEvaluationException(e);
        }
    }
}
