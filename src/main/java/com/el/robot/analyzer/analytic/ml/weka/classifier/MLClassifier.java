package com.el.robot.analyzer.analytic.ml.weka.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.*;
import weka.classifiers.functions.*;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.lazy.LWL;
import weka.classifiers.meta.*;
import weka.classifiers.misc.InputMappedClassifier;
import weka.classifiers.rules.*;
import weka.classifiers.trees.*;

import java.util.*;
import java.util.stream.Collectors;

public enum MLClassifier {
    BAYES_NET(false, true, false, 50) {
        @Override
        public Classifier getInstance() {
            return new BayesNet();
        }
    },
    NAIVE_BAYES(false, true, false, 50) {
        @Override
        public Classifier getInstance() {
            return new NaiveBayes();
        }
    },
    NAIVE_BAYES_MULTINOMIAL(false, true, false, 50) {
        @Override
        public Classifier getInstance() {
            return new NaiveBayesMultinomial();
        }
    },
    NAIVE_BAYES_MULTINOMIAL_TEXT(false, true, true, 50) {
        @Override
        public Classifier getInstance() {
            return new NaiveBayesMultinomialText();
        }
    },
    NAIVE_BAYES_UPDATEABLE(false, true, false, 50) {
        @Override
        public Classifier getInstance() {
            return new NaiveBayesUpdateable();
        }
    },
    NAIVE_BAYES_MULTINOMIAL_UPDATEABLE(false, true, false, 50) {
        @Override
        public Classifier getInstance() {
            return new NaiveBayesMultinomialUpdateable();
        }
    },
    SMO(false, true, false, 50) {
        @Override
        public Classifier getInstance() {
            return new SMO();
        }
    },
    SMO_REG(true, false, false, 50) {
        @Override
        public Classifier getInstance() {
            return new SMOreg();
        }
    },
    LOGISTIC(false, true, true, 50) {
        @Override
        public Classifier getInstance() {
            return new Logistic();
        }
    },
    SIMPLE_LOGISTIC(false, true, true, 50) {
        @Override
        public Classifier getInstance() {
            return new SimpleLogistic();
        }
    },
    VOTED_PERCEPTION(false, true, false, true, 50) {
        @Override
        public Classifier getInstance() {
            return new VotedPerceptron();
        }
    },
    GAUSSIAN_PROCESSES(true, false, false, 20) {
        @Override
        public Classifier getInstance() {
            return new GaussianProcesses();
        }
    },
    LINEAR_REGRESSION(true, false, true, 15) {
        @Override
        public Classifier getInstance() {
            return new LinearRegression();
        }
    },
    SIMPLE_LINEAR_REGRESSION(true, false, true, 15) {
        @Override
        public Classifier getInstance() {
            return new SimpleLinearRegression();
        }
    },
    MULTI_LAYER_PERCEPTION(true, true, true, 30) {
        @Override
        public Classifier getInstance() {
            return new MultilayerPerceptron();
        }
    },
    SGD(false, true, false, false, 30) {
        @Override
        public Classifier getInstance() {
            return new SGD();
        }
    },
    SGD_TEXT(false, true, false, true, 30) {
        @Override
        public Classifier getInstance() {
            return new SGDText();
        }
    },
    RANDOM_COMMITTEE(true, true, true, 30) {
        @Override
        public Classifier getInstance() {
            return new RandomCommittee();
        }
    },
    STAKING(true, true, true, 20) {
        @Override
        public Classifier getInstance() {
            return new Stacking();
        }
    },
    VOTE(true, true, true, 20) {
        @Override
        public Classifier getInstance() {
            return new Vote();
        }
    },
    REGRESSION_BY_DISCRETIZATION(true, false, true, 20) {
        @Override
        public Classifier getInstance() {
            return new RegressionByDiscretization();
        }
    },
    ADA_BOOST_M1(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new AdaBoostM1();
        }
    },
    ADDITIVE_REGRESSION(true, false, true, 25) {
        @Override
        public Classifier getInstance() {
            return new AdditiveRegression();
        }
    },
    ATTRIBUTE_SELECTION_CLASSIFIER(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new AttributeSelectedClassifier();
        }
    },
    BAGGING(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new Bagging();
        }
    },
    CLASSIFICATION_VIA_REGRESSION(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new ClassificationViaRegression();
        }
    },
    /*COST_SENSITIVE_CLASSIFIER(false, true, false, 25) {
        @Override
        public Classifier getInstance() {
            return new CostSensitiveClassifier();
        }
    },*/
    CV_PARAMETER_SELECTION(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new CVParameterSelection();
        }
    },
    FILTERED_CLASSIFIER(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new FilteredClassifier();
        }
    },
    ITERATIVE_CLASSIFIER_OPTIMIZER(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new IterativeClassifierOptimizer();
        }
    },
    LOGIT_BOOST(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new LogitBoost();
        }
    },
    MULTI_CLASS_CLASSIFIER(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new MultiClassClassifier();
        }
    },
    MULTI_SCHEMA(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new MultiScheme();
        }
    },
    RAMDOMIZABLE_FILTERED_CLASSIFIER(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new RandomizableFilteredClassifier();
        }
    },
    RANDOM_SUB_SPACE(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new RandomSubSpace();
        }
    },
    INPUT_MAPPED_CLASSIFIER(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new InputMappedClassifier();
        }
    },
    /*SERIALIZED_CLASSIFIER(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new SerializedClassifier();
        }
    },*/
    IBK(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new IBk();
        }
    },
    K_STAR(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new KStar();
        }
    },
    LWL(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new LWL();
        }
    },
    WEIGHTED_INSTANCES_HANDLER_WRAPPER(true, true, true, 20) {
        @Override
        public Classifier getInstance() {
            return new WeightedInstancesHandlerWrapper();
        }
    },
    DECISION_TABLE(true, true, true, 30) {
        @Override
        public Classifier getInstance() {
            return new DecisionTable();
        }
    },
    J_RIP(false, true, true, 30) {
        @Override
        public Classifier getInstance() {
            return new JRip();
        }
    },
    M5_RULES(true, false, true, 25) {
        @Override
        public Classifier getInstance() {
            return new M5Rules();
        }
    },
    ONE_R(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new OneR();
        }
    },
    PART(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new PART();
        }
    },
    ZERO_R(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new ZeroR();
        }
    },
    DECISION_STUMP(true, true, true, 30) {
        @Override
        public Classifier getInstance() {
            return new DecisionStump();
        }
    },
    J48(false, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new J48();
        }
    },
    HOEFFDING_TREE(false, true, false, 30) {
        @Override
        public Classifier getInstance() {
            return new HoeffdingTree();
        }
    },
    LMT(false, true, true, 30) {
        @Override
        public Classifier getInstance() {
            return new LMT();
        }
    },
    M5P(true, false, true, 30) {
        @Override
        public Classifier getInstance() {
            return new M5P();
        }
    },
    RANDOM_FOREST(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new RandomForest();
        }
    },
    RANDOM_TREE(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new RandomTree();
        }
    },
    REP_TREE(true, true, true, 25) {
        @Override
        public Classifier getInstance() {
            return new REPTree();
        }
    },;

    private int effectivenessIndex;
    private boolean supportDateFeature;
    private boolean supportNumericPrediction;
    private boolean supportBinaryPrediction;
    private boolean supportMultiValuesNominalPrediction;

    private static List<MLClassifier> numericPredictingClassifiers = new ArrayList<>();
    private static List<MLClassifier> binaryPredictingClassifiers = new ArrayList<>();
    private static List<MLClassifier> multiValuesNominalPredictingClassifiers = new ArrayList<>();
    private static List<MLClassifier> dateFeatureSupportedClassifiers = new ArrayList<>();

    static {
        for (MLClassifier mlClassifier : MLClassifier.values()) {
            if (mlClassifier.isSupportDateFeature()) {
                dateFeatureSupportedClassifiers.add(mlClassifier);
            }

            if (mlClassifier.isSupportNumericPrediction()) {
                numericPredictingClassifiers.add(mlClassifier);
            }

            if (mlClassifier.isSupportBinaryPrediction()) {
                binaryPredictingClassifiers.add(mlClassifier);
            }

            if (mlClassifier.isSupportMultiValuesNominalPrediction()) {
                multiValuesNominalPredictingClassifiers.add(mlClassifier);
            }
        }

        Collections.sort(numericPredictingClassifiers, new MlClassifierComparator());
        Collections.sort(binaryPredictingClassifiers, new MlClassifierComparator());
        Collections.sort(dateFeatureSupportedClassifiers, new MlClassifierComparator());
    }

    public abstract Classifier getInstance();

    MLClassifier(boolean supportNumericPrediction, boolean supportBinaryPrediction, boolean supportDateFeature, int effectivenessIndex) {
        this(supportNumericPrediction, supportBinaryPrediction, supportBinaryPrediction, supportDateFeature, effectivenessIndex);
    }

    MLClassifier(boolean supportNumericPrediction, boolean supportBinaryPrediction, boolean supportMultiValuesNominalPrediction, boolean supportDateFeature, int effectivenessIndex) {
        this.supportNumericPrediction = supportNumericPrediction;
        this.supportBinaryPrediction = supportBinaryPrediction;
        this.supportMultiValuesNominalPrediction = supportMultiValuesNominalPrediction;
        this.supportDateFeature = supportDateFeature;
        this.effectivenessIndex = effectivenessIndex;
    }

    /**
     * Classifier Comparator which uses classifier effectiveness for comparison.
     */
    static class MlClassifierComparator implements Comparator<MLClassifier> {
        public int compare(MLClassifier o1, MLClassifier o2) {
            return o1.getEffectivenessIndex() - o2.getEffectivenessIndex();
        }
    }

    public enum  MlClassifierFilter {
        NUMERIC_SUPPORTED {
            boolean isSupported(MLClassifier classifier) {
                return classifier.isSupportNumericPrediction();
            }
        },
        BINARY_SUPPORTED {
            boolean isSupported(MLClassifier classifier) {
                return classifier.isSupportBinaryPrediction();
            }
        },
        MULTI_NOMINAL_SUPPORTED {
            boolean isSupported(MLClassifier classifier) {
                return classifier.isSupportMultiValuesNominalPrediction();
            }
        },
        DATE_SUPPORTED {
            @Override
            boolean isSupported(MLClassifier classifier) {
                return classifier.isSupportDateFeature();
            }
        };

        abstract boolean isSupported(MLClassifier classifier);

    }

    public int getEffectivenessIndex() {
        return effectivenessIndex;
    }

    public boolean isSupportNumericPrediction() {
        return supportNumericPrediction;
    }

    public boolean isSupportDateFeature() {
        return supportDateFeature;
    }

    public boolean isSupportBinaryPrediction() {
        return supportBinaryPrediction;
    }

    public boolean isSupportMultiValuesNominalPrediction() {
        return supportMultiValuesNominalPrediction;
    }

    public static Collection<MLClassifier> getNumericPredictingClassifiers() {
        return numericPredictingClassifiers;
    }

    public static Collection<MLClassifier> getDateFeatureSupportedClassifiers() {
        return dateFeatureSupportedClassifiers;
    }

    public static List<MLClassifier> getBinaryPredictingClassifiers() {
        return binaryPredictingClassifiers;
    }

    public static List<MLClassifier> getMultiValuesNominalPredictingClassifiers() {
        return multiValuesNominalPredictingClassifiers;
    }

    public static List<MLClassifier> getMLClassifiers(MlClassifierFilter... mlClassifierFilters) {
        List<MlClassifierFilter> mlClassifierFilterList = Arrays.asList(mlClassifierFilters);
        return Arrays.stream(MLClassifier.values())
                .filter(mlClassifier -> mlClassifierFilterList.stream().allMatch(mlClassifierFilter -> mlClassifierFilter.isSupported(mlClassifier)))
                .collect(Collectors.toList());
    }
}
