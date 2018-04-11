package com.el.robot.analyzer.analytic.ml.weka.classifier;

import com.el.betting.common.CollectionUtil;
import com.el.robot.analyzer.analytic.ml.exceptions.ClassificationFailedException;
import com.el.robot.analyzer.analytic.ml.services.MLPredictionService;
import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.el.robot.analyzer.analytic.ml.weka.dataset.WekaDatasetBuilder.addInstance;

@Service
public class WekaPredictionService implements MLPredictionService{

    @Override
    public double predictNumericValue(Classifier classifier, ArrayList<Attribute> attributes, Object... instanceValues) throws ClassificationFailedException {
        Instances testInstances = new Instances("", attributes, 1);
        addInstance(testInstances, instanceValues);
        testInstances.setClassIndex(testInstances.numAttributes() - 1);
        try {
            return classifier.classifyInstance(testInstances.firstInstance());
        } catch (Exception e) {
            throw new ClassificationFailedException(e);
        }
    }

    @Override
    public String predictNominalValue(Classifier classifier, ArrayList<Attribute> attributes, Object... instanceValues) throws ClassificationFailedException {
        Instances testInstances = new Instances("", attributes, 1);
        addInstance(testInstances, instanceValues);
        testInstances.setClassIndex(testInstances.numAttributes() - 1);
        try {
            double predictionIndex = classifier.classifyInstance(testInstances.firstInstance());
            return testInstances.classAttribute().value((int) predictionIndex);
        } catch (Exception e) {
            throw new ClassificationFailedException(e);
        }
    }

    @Override
    public Map<String, Double> measureDistribution(Classifier classifier, ArrayList<Attribute> attributes, Object... instanceValues) throws ClassificationFailedException {
        Instances testInstances = new Instances("", attributes, 1);
        addInstance(testInstances, instanceValues);
        testInstances.setClassIndex(testInstances.numAttributes() - 1);
        try {
            Map<String, Double> distribution = new HashMap<>();
            double[] doubles = classifier.distributionForInstance(testInstances.firstInstance());
            for (int i = 0; i < doubles.length; i++) {
                String value = testInstances.classAttribute().value(i);
                distribution.put(value, doubles[i]);
            }

            return CollectionUtil.sortByValue(distribution, false);
        } catch (Exception e) {
            throw new ClassificationFailedException(e);
        }
    }
}
