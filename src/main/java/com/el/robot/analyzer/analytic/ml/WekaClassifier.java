package com.el.robot.analyzer.analytic.ml;

import weka.classifiers.Classifier;
import weka.core.*;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;

import java.util.ArrayList;

public class WekaClassifier {

    public void classify() throws Exception {
        Instances train = initTrainingData();
        train.setClassIndex(2);
        Instances test = getTestData();
        test.setClassIndex(2);

        // train classifier
        Classifier classifier = new J48();
        classifier.buildClassifier(train);



        DenseInstance instance1 = new DenseInstance(3);
        instance1.setValue(0, 2.5);
        instance1.setValue(1, 1.5);
        instance1.setValue(2, "First");
        double v = classifier.classifyInstance(test.firstInstance());
        System.out.println(v);

        // evaluate classifier and print some statistics
        Evaluation evaluation = new Evaluation(train);
        evaluation.evaluateModel(classifier, test);

        System.out.println(evaluation.toSummaryString("\nResults\n======\n", false));
    }

    private Instances initTrainingData() {
        //create attributes
        Attribute length = new Attribute("length");
        length.isNumeric();
        Attribute weight = new Attribute("weight");
        weight.isNumeric();

        ArrayList<String> positionValues = new ArrayList<>();
        positionValues.add("First");
        positionValues.add("Second");
        positionValues.add("Third");

        Attribute position = new Attribute("position", positionValues);
        position.isNominal();

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(length);
        attributes.add(weight);
        attributes.add(position);
        Instances instances = new Instances("mydataset", attributes, 0);

        DenseInstance instance1 = new DenseInstance(instances.numAttributes());
        instance1.setValue(length, 2.5);
        instance1.setValue(weight, 1.5);
        instance1.setValue(position, "First");
        instances.add(instance1);

        DenseInstance instance2 = new DenseInstance(instances.numAttributes());
        instance2.setValue(length, 3.5);
        instance2.setValue(weight, 2.5);
        instance2.setValue(position, "Second");
        instances.add(instance2);

        DenseInstance instance3 = new DenseInstance(instances.numAttributes());
        instance3.setValue(length, 4.5);
        instance3.setValue(weight, 3.5);
        instance3.setValue(position, "Third");
        instances.add(instance3);

        return instances;
    }

    public Instances getTestData() {
        //create attributes
        Attribute length = new Attribute("length");
        length.isNumeric();
        Attribute weight = new Attribute("weight");
        weight.isNumeric();

        ArrayList<String> positionValues = new ArrayList<>();
        positionValues.add("First");
        positionValues.add("Second");
        positionValues.add("Third");

        Attribute position = new Attribute("position", positionValues);
        position.isNominal();

        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(length);
        attributes.add(weight);
        attributes.add(position);
        Instances instances = new Instances("mydataset", attributes, 0);

        DenseInstance instance1 = new DenseInstance(instances.numAttributes());
        instance1.setValue(length, 2.5);
        instance1.setValue(weight, 1.5);
        instance1.setValue(position, "First");
        instances.add(instance1);
        return instances;
    }

    public static void main(String[] args) throws Exception {
        WekaClassifier wekaClassifier = new WekaClassifier();
        wekaClassifier.classify();
    }
}
