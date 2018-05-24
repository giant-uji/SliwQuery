package es.uji.esansano.sliwquery.ml;

import es.uji.esansano.sliwquery.models.Sample;
import es.uji.esansano.sliwquery.models.User;
import weka.classifiers.Classifier;

import java.util.List;

public interface MLService {

    List<Classifier> buildClassifiers(User user, List<Sample> samples, boolean log);
    String classify(User user, Sample sample, boolean log);

}
