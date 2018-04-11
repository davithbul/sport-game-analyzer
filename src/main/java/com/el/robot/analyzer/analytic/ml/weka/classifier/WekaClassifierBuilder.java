package com.el.robot.analyzer.analytic.ml.weka.classifier;

import com.el.robot.analyzer.analytic.ml.model.DateFeature;
import com.el.robot.analyzer.analytic.ml.model.Feature;
import com.el.robot.analyzer.analytic.ml.model.NominalFeature;
import com.el.robot.analyzer.analytic.ml.services.MLPredictionService;
import com.el.robot.analyzer.analytic.ml.weka.dataset.WekaDatasetBuilder;
import weka.classifiers.Classifier;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.SMO;
import weka.core.*;

import java.time.LocalDateTime;
import java.util.*;

import static com.el.robot.analyzer.analytic.ml.weka.dataset.WekaDatasetBuilder.addInstance;

public class WekaClassifierBuilder {

    public Instances buildDataSet() throws Exception {
        // Define each attribute (or column), and give it a numerical column number
        // Likely, a better design wouldn't require the column number, but
        // would instead get it from the index in the container
        Attribute attribute1 = new Attribute("houseSize", 0);
        Attribute attribute2 = new Attribute("lotSize", 1);
        Attribute attribute3 = new Attribute("bedrooms", 2);
        Attribute attribute4 = new Attribute("granite", 3);
        Attribute attribute5 = new Attribute("bathroom", 4);
        Attribute attribute6 = new Attribute("sellingPrice", Arrays.asList("true", "false"), 5);

        // Each element must be added to a FastVector, a custom
        // container used in this version of Weka.
        // Later versions of Weka corrected this mistake by only
        // using an ArrayList
        ArrayList<Attribute> attrs = new ArrayList<>();
        attrs.add(attribute1);
        attrs.add(attribute2);
        attrs.add(attribute3);
        attrs.add(attribute4);
        attrs.add(attribute5);
        attrs.add(attribute6);

        // Each data instance needs to create an Instance class
        // The constructor requires the number of columns that
        // will be defined.  In this case, this is a good design,
        // since you can pass in empty values where they exist.
        Instance instance1 = new DenseInstance(6);
        instance1.setValue(attribute1, 3529);
        instance1.setValue(attribute2, 9191);
        instance1.setValue(attribute3, 6);
        instance1.setValue(attribute4, 0);
        instance1.setValue(attribute5, 0);
        instance1.setValue(attribute6, "true");

        Instance instance2 = new DenseInstance(6);
        instance2.setValue(attribute1, 3529);
        instance2.setValue(attribute2, 9191);
        instance2.setValue(attribute3, 6);
        instance2.setValue(attribute4, 0);
        instance2.setValue(attribute5, 0);
        instance2.setValue(attribute6, "true");


        // Each Instance has to be added to a larger container, the
        // Instances class.  In the constructor for this class, you
        // must give it a name, pass along the Attributes that
        // are used in the data set, and the number of
        // Instance objects to be added.  Again, probably not ideal design
        // to require the number of objects to be added in the constructor,
        // especially since you can specify 0 here, and then add Instance
        // objects, and it will return the correct value later (so in
        // other words, you should just pass in '0' here)
        Instances dataset = new Instances("housePrices", attrs, 7);
        dataset.add(instance1);
        dataset.add(instance2);

        // In the Instances class, we need to set the column that is
        // the output (aka the dependent variable).  You should remember
        // that some data mining methods are used to predict an output
        // variable, and regression is one of them.
        dataset.setClassIndex(dataset.numAttributes() - 1);

        Instances newOne = new Instances("housePrices", attrs, 7);
        newOne.setClassIndex(dataset.numAttributes() - 1);

        Instance predictValue = new DenseInstance(5);
        instance2.setValue(0, 100);
        instance2.setValue(1, 350);
        instance2.setValue(2, 6);
        instance2.setValue(3, 3);
        instance2.setValue(4, 5);
        newOne.add(predictValue);

        SMO classifier = new SMO();
        classifier.buildClassifier(dataset);

        double v = classifier.classifyInstance(dataset.firstInstance());

        System.out.println(v);
        return dataset;
    }

    public double doRegression(Instances dataset) throws Exception {
        // Create the LinearRegression model, which is the data mining
        // model we're using in this example
        LinearRegression linearRegression = new LinearRegression();

        // This method does the "magic", and will compute the regression
        // model.  It takes the entire dataset we've defined to this point
        // When this method completes, all our "data mining" will be complete
        // and it is up to you to get information from the results
        linearRegression.buildClassifier(dataset);

        // We are most interested in the computed coefficients in our model,
        // since those will be used to compute the output values from an
        // unknown data instance.
        double[] coefficients = linearRegression.coefficients();

        // Using the values from my house (from the first article), we
        // plug in the values and multiply them by the coefficients
        // that the regression model created.  Note that we skipped
        // coefficient[5] as that is 0, because it was the output
        // variable from our training data
        double myHouseValue = (coefficients[0] * 3198) +
                (coefficients[1] * 9669) +
                (coefficients[2] * 5) +
                (coefficients[3] * 3) +
                (coefficients[4] * 1) +
                coefficients[6];

        return myHouseValue;
    }

    public double predictValue(Instances dataset) throws Exception {
        // Create the LinearRegression model, which is the data mining
        // model we're using in this example
        SMO classifier = new SMO();

        // This method does the "magic", and will compute the regression
        // model.  It takes the entire dataset we've defined to this point
        // When this method completes, all our "data mining" will be complete
        // and it is up to you to get information from the results
        classifier.buildClassifier(dataset);

        // We are most interested in the computed coefficients in our model,
        // since those will be used to compute the output values from an
        // unknown data instance.
        Instance instance2 = new DenseInstance(5);
        instance2.setValue(0, 3529);
        instance2.setValue(1, 9191);
        instance2.setValue(2, 6);
        instance2.setValue(3, 0);
        instance2.setValue(4, 0);
//        instance2.setValue(5, 205000);

        return classifier.classifyInstance(instance2);
    }

    public static void main(String[] args) throws Exception {
        WekaDatasetBuilder datasetBuilder = new WekaDatasetBuilder();

        Feature[] features = {
                new DateFeature("startDate"),
                new Feature("houseSize"),
                new Feature("lotSize"),
                new Feature("bedrooms"),
                new Feature("bathroom"),
                new Feature("sellingPrice")
//                new NominalFeature("sold", "yes", "no")
        };

        Instances dataSet = datasetBuilder.buildDataSet(
                "housePrices",
                features,
                new Object[]{LocalDateTime.now(), 4500, 5800, 12, 35, 1000},
                new Object[]{LocalDateTime.now(), 4600, 5900, 13, 36, 2000},
                new Object[]{LocalDateTime.now(), 4700, 6000, 14, 37, 3000},
                new Object[]{LocalDateTime.now(), 4800, 6100, 15, 38, 4000},
                new Object[]{LocalDateTime.now(), 4900, 6200, 16, 39, 5000},
                new Object[]{LocalDateTime.now(), 5000, 6200, 16, 39, 5000},
                new Object[]{LocalDateTime.now(), 5100, 6300, 16, 39, 5000},
                new Object[]{LocalDateTime.now(), 5200, 6400, 16, 39, 5000},
                new Object[]{LocalDateTime.now(), 5300, 6500, 16, 39, 5000},
                new Object[]{LocalDateTime.now(), 5400, 6600, 20, 70, 9000},
                new Object[]{LocalDateTime.now(), 5400, 6600, 20, 70, 9000}
        );

        Classifier classifier = MLClassifier.DECISION_STUMP.getInstance();
        MLPredictionService mlPredictionService = new WekaPredictionService();

        classifier.buildClassifier(dataSet);

        ArrayList<Attribute> attributes = new ArrayList<>(Collections.list(dataSet.enumerateAttributes()));
        attributes.add(dataSet.classAttribute());

        Instances testInstances = new Instances("", attributes, 0);
        addInstance(testInstances, LocalDateTime.now(), 4800, 6100, 15, 38, 0);
        testInstances.setClassIndex(testInstances.numAttributes() - 1);
        double v = classifier.classifyInstance(testInstances.firstInstance());
        System.out.println("classify Instance: " + v);

        double[] doubles = classifier.distributionForInstance(testInstances.firstInstance());
        System.out.println(Arrays.toString(doubles));

        WekaModelEvaluator wekaModelEvaluator = new WekaModelEvaluator();
        wekaModelEvaluator.evaluateModel(classifier, dataSet, testInstances);


        MLClassifier.getDateFeatureSupportedClassifiers().forEach(mlClassifier -> {
            try {
                Classifier classifierInstance = mlClassifier.getInstance();
                classifierInstance.buildClassifier(dataSet);
                Object predictedValue = mlPredictionService.predictNumericValue(classifierInstance, attributes, LocalDateTime.now(), 4800, 6100, 15, 38, 0);
                System.out.println(mlClassifier + " - " + predictedValue);
                double modelCorrectness = wekaModelEvaluator.crossEvaluateModel(mlClassifier.getInstance(), dataSet, 2);
                System.out.println(mlClassifier + " - " + modelCorrectness);
            } catch (Exception e) {
                System.out.println(mlClassifier + " - " + e.getMessage());
            }
        });

    }
}
