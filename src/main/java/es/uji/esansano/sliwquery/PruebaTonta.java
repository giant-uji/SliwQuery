package es.uji.esansano.sliwquery;

import es.uji.esansano.sliwquery.ml.MLServiceImpl;
import es.uji.esansano.sliwquery.models.Device;
import es.uji.esansano.sliwquery.models.Sample;
import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.query.SliwQuery;
import org.elasticsearch.common.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PruebaTonta {

    private static final MLServiceImpl mlService = new MLServiceImpl();
    private static final DateTime FROM = new DateTime(2018, 5, 13, 0, 0);
    private static final DateTime TO = new DateTime(2018, 5, 23, 22, 30);
//    private static final String USER = "Emilio Prueba";
//    private static final String USER = "Prueba Oscar";
//    private static final String USER = "Prueba Raul";
    private static final String USER = "arturo";

    public static void main(String[] args) {
//        List<Device> devices = SliwQuery.getDevices();
//        printDevices(devices);

        List<User> users = SliwQuery.getUsers();
        printUsers(users);

        Map<String, User> nameUserMap = new HashMap<>();
        for (User user: users) {
            nameUserMap.put(user.getName(), user);
        }

        printLocations(nameUserMap.get(USER), FROM, TO, true);
    }

    private static void printLocations(User user, DateTime from, DateTime to, boolean detailed) {
        List<Sample> samples = SliwQuery.getSamples(user.getId(), from, to);
        if (detailed) {
            printLocations(user, samples);
        }
        if (samples != null && samples.size() > 1) {
            Sample prevSample = samples.get(0);
            DateTime startDate = prevSample.getDate();
            int scanResults = 0;
            int invalidatedSamples = 0;
            if (!prevSample.isValid()) {
                invalidatedSamples++;
                if (prevSample.getScanResults().size() > 0 && !prevSample.isValid()) {
                    scanResults++;
                }
            }
            for (int i = 1; i < samples.size() - 1; i++) {
                String prevLoc = prevSample.getLocation();
                String currLoc = samples.get(i).getLocation();
                if (!samples.get(i).isValid()) {
                    invalidatedSamples++;
                    if (samples.get(i).getScanResults().size() > 0) {
                        scanResults++;
                    }
                }
                if (!prevLoc.equals(currLoc) || i == samples.size() - 1) {
                    DateTime stopDate = samples.get(i).getDate();
                    String date1 = startDate.toString("dd/MM HH:mm:ss");
                    String date2 = stopDate.toString("dd/MM HH:mm:ss");
                    System.out.println("From " + date1 + " to " + date2 + " in " + prevSample.getLocation());
                    prevSample = samples.get(i);
                    startDate = prevSample.getDate();
                }
            }
            String date1 = startDate.toString("dd/MM HH:mm:ss");
            String date2 = samples.get(samples.size() - 1).getDate().toString("dd/MM HH:mm:ss");
            System.out.println("From " + date1 + " to " + date2 + " in " + samples.get(samples.size() - 1).getLocation());
            System.out.println(String.format("There are %d normal samples (valid==false)", invalidatedSamples));
            System.out.println(String.format("%d samples have scan readings", scanResults));
        } else {
            System.out.println("Not enough samples :(");
        }
    }

    private static void printDevices(List<Device> devices) {
        for (Device device: devices) {
            System.out.println(device);
        }
    }

    private static void printLocations(User user, List<Sample> samples) {
        for (Sample sample : samples) {
            String date = sample.getDate().toString("dd/MM HH:mm:ss");
            String localPrediction = getLocalPrediction(user, sample);
            System.out.println(String.format("%s -> %12s (L: %s)", date, sample.getLocation(), localPrediction));
            System.out.println();
        }
    }

    private static void printUsers(List<User> users) {
        for (User user: users) {
            System.out.println(user);
        }
    }

    private static String getLocalPrediction(User user, Sample sample) {
        return mlService.classify(user, sample);
    }
}
