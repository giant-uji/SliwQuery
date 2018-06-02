package es.uji.esansano.sliwquery.utils;

import es.uji.esansano.sliwquery.ml.MLServiceImpl;
import es.uji.esansano.sliwquery.models.*;
import org.elasticsearch.common.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Utils {

    private static final MLServiceImpl mlService = new MLServiceImpl();


    public static void printDevices(List<Device> devices) {
        for (Device device: devices) {
            System.out.println(device);
        }
    }

    public static void printLocations(User user, Report report, boolean detailed) {
        if (report != null && report.getSamples() != null && report.getSamples().size() > 0) {
            for (Sample sample : report.getSamples()) {
                String date = sample.getDate().toString("dd/MM HH:mm:ss");
                String localPrediction = mlService.getLocalPrediction(user, sample, detailed);
                System.out.println(String.format("%s -> %12s [%s]", date, sample.getLocation(), localPrediction));
                if (detailed) {
                    System.out.println("\n");
                }
            }
        } else {
            System.out.println("No hay localizaciones disponibles.");
        }
        System.out.println("\n");


    }

    public static void printUsers(Collection<User> users) {
        System.out.println("Relación de usuarios en la base de datos.");
        for (User user: users) {
            System.out.println(user);
        }
        System.out.println();
    }

    public static void printReport(Report report, float threshold, DateTime from, DateTime to) {
        report.printReport(threshold, from, to);
    }

    public static String getReport(Report report, float threshold, DateTime from, DateTime to) {
        String reportString = report.getReport(threshold, from, to);
        return reportString;
    }


    public static List<Interval> getIntervals(String userName) {
        List<Interval> intervals = new ArrayList<>();
        String fileName = "data/" + userName + ".csv";
        try {
            BufferedReader intervalCsv = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = intervalCsv.readLine()) != null) {
                String[] lineData = line.split(",");
                intervals.add(new Interval(
                        lineData[0], lineData[1], lineData[2]
                ));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return intervals;
    }
}
