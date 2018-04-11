package com.el.robot.analyzer.analytic.ml.weka.dataset;

import com.el.betting.common.DateUtils;
import com.el.robot.analyzer.analytic.ml.factory.DatasetBuilder;
import com.el.robot.analyzer.analytic.ml.model.DateFeature;
import com.el.robot.analyzer.analytic.ml.model.Feature;
import com.el.robot.analyzer.analytic.ml.model.NominalFeature;
import com.google.common.base.Preconditions;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.text.ParseException;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class WekaDatasetBuilder extends DatasetBuilder {

    @Override
    public Instances buildDataSet(String name, Feature[] features, Object[]... valuesList) {
        // Define each attribute (or column), and give it a numerical column number
        // Likely, a better design wouldn't require the column number, but
        // would instead get it from the index in the container
        ArrayList<Attribute> attributes = IntStream.range(0, features.length)
                .mapToObj(index -> {
                    Feature feature = features[index];
                    Attribute attribute;
                    if (feature instanceof NominalFeature) {
                        attribute = new Attribute(feature.getName(), ((NominalFeature) feature).getNominals(), index);
                    } else if (feature instanceof DateFeature) {
                        attribute = new Attribute(feature.getName(), ((DateFeature) feature).getDateFormat(), index);
                    } else {
                        attribute = new Attribute(feature.getName(), index);
                    }
                    return attribute;
                }).collect(Collectors.toCollection(ArrayList::new));

        // Each Instance has to be added to a larger container, the
        // Instances class.  In the constructor for this class, you
        // must give it a name, pass along the Attributes that
        // are used in the data set, and the number of
        // Instance objects to be added.  Again, probably not ideal design
        // to require the number of objects to be added in the constructor,
        // especially since you can specify 0 here, and then add Instance
        // objects, and it will return the correct value later (so in
        // other words, you should just pass in '0' here)
        Instances dataset = new Instances(name, attributes, valuesList.length);
        for (Object[] values : valuesList) {
            assert values.length== features.length;
            addInstance(dataset, values);
        }

        // In the Instances class, we need to set the column that is
        // the output (aka the dependent variable).  You should remember
        // that some data mining methods are used to predict an output
        // variable, and regression is one of them.
        dataset.setClassIndex(dataset.numAttributes() - 1);
        return dataset;
    }


    public static Instance addInstance(Instances dataset, Object... values) {
        Preconditions.checkArgument(values.length > 1);

        DenseInstance instance = new DenseInstance(values.length);
        instance.setDataset(dataset);
        for (int i = 0; i < values.length; i++) {
            if(values[i] instanceof Number) {
                if(values[i] instanceof Integer) {
                    instance.setValue(i, (double) (Integer) values[i]);
                } else {
                    instance.setValue(i, (double) values[i]);
                }
            } else if(values[i] instanceof Temporal) {
                try {
                    instance.setValue(dataset.attribute(i), dataset.attribute(i).parseDate(DateUtils.format((Temporal) values[i], dataset.attribute(i).getDateFormat())));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                instance.setValue(i, (String)values[i]);
            }
        }

        dataset.add(instance);
        return instance;
    }
}
