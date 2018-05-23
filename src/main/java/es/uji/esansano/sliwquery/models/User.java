package es.uji.esansano.sliwquery.models;

import es.uji.esansano.sliwquery.models.Location;
import weka.classifiers.Classifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.List;

public class User {

    private String id;
    private String name;
    private List<Location> locations;
    private boolean configured;
    private List<String> bssids; // BSSIDs used in classifiers
    private List<String> classifiers; // Classifiers in base64
    private transient Classifier[] wekaClassifiers;

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public List<String> getBssids() {
        return bssids;
    }

    public void setBssids(List<String> bssids) {
        this.bssids = bssids;
    }

    public List<String> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(List<String> classifiers) {
        this.classifiers = classifiers;
    }

    public Classifier[] getWekaClassifiers() {
        if (wekaClassifiers == null) {
            wekaClassifiers = new Classifier[5];
            for (int i = 0; i < classifiers.size(); i++) {
                wekaClassifiers[i] = fromBase64(classifiers.get(i));
            }
        }
        return wekaClassifiers;
    }

    private static Classifier fromBase64(String base64EncodedClassifier) {
        try {
            byte[] bytes = Base64.getDecoder().decode(base64EncodedClassifier);
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Classifier classifier = (Classifier) ois.readObject();
            ois.close();
            bais.close();
            return classifier;
        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%12s · id: %s · configured: %s", getName(), getId(), isConfigured());
    }
}