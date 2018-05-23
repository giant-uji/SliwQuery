package es.uji.esansano.sliwquery.ml;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TrainingSetBuilder {

    private List<Attribute> attributes;
    private Attribute classAttribute;

    public TrainingSetBuilder setAttributes(List<String> names) {
        this.attributes = names.stream().map(Attribute::new).collect(Collectors.toList());
        return this;
    }

    public TrainingSetBuilder setAttributes(String... names) {
        this.attributes = Arrays.stream(names).map(Attribute::new).collect(Collectors.toList());
        return this;
    }

    public TrainingSetBuilder setClassAttribute(String name, List<String> values) {
        FastVector fvClassValues = new FastVector(values.size());
        values.stream().forEach(fvClassValues::addElement);
        this.classAttribute = new Attribute(name, fvClassValues);
        return this;
    }

    public TrainingSetBuilder setClassAttribute(String name, String... values) {
        FastVector fvClassValues = new FastVector(values.length);
        Arrays.stream(values).forEach(fvClassValues::addElement);
        this.classAttribute = new Attribute(name, fvClassValues);
        return this;
    }

    public Instances build(String name, int capacity) {
        FastVector fvAttributes = new FastVector(attributes.size()+1);
        attributes.forEach(fvAttributes::addElement);
        fvAttributes.addElement(classAttribute);

        Instances trainingSet = new Instances(name, fvAttributes, capacity);
        trainingSet.setClassIndex(fvAttributes.size()-1);

        return trainingSet;
    }

}
