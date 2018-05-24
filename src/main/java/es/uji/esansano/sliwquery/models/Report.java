package es.uji.esansano.sliwquery.models;

import org.elasticsearch.common.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Report {

    private String user;
    private DateTime fromDate;
    private DateTime toDate;
    private List<LocationInterval> intervals = new ArrayList<>();
    private List<Sample> samples;

    public Report() {
    }

    public Report(String user, DateTime fromDate, DateTime toDate) {
        this.user = user;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public void setSamples(List<Sample> samples) {
        this.samples = samples;
        buildReport();
    }

    private void buildReport() {
        if (samples != null && samples.size() > 0) {
            int firstValidSample = 0;
            while (firstValidSample < samples.size() && samples.get(firstValidSample).getScanResults().size() == 0) {
                firstValidSample++;
            }

            if (firstValidSample < samples.size()) {
                String currentLocation = samples.get(firstValidSample).getLocation();
                DateTime startDate = samples.get(firstValidSample).getDate();

                int sampleIndex = firstValidSample + 1;
                boolean closeInterval;

                while (sampleIndex < samples.size()) {
                    if (samples.get(sampleIndex).getScanResults().size() != 0) {
                        closeInterval = !samples.get(sampleIndex).getLocation().equals(currentLocation) || sampleIndex == samples.size() - 1;
                        if (closeInterval) {
                            addInterval(currentLocation, startDate, samples.get(sampleIndex).getDate());
                            startDate = samples.get(sampleIndex).getDate();
                            currentLocation = samples.get(sampleIndex).getLocation();
                        }
                    }
                    sampleIndex++;
                }
            }
        }
    }

    private void addInterval(String location, DateTime from, DateTime to) {
        intervals.add(new LocationInterval(location, from, to));
    }


    public void printReport(float filterThreshold, DateTime from, DateTime to) {
        if (user != null) {
            System.out.println(String.format("\n\nInforme para el usuario: %s", user));
            if (from == null) from = fromDate;
            if (to == null) to = toDate;
            String date1 = from.toString("dd/MM HH:mm:ss");
            String date2 = to.toString("dd/MM HH:mm:ss");
            System.out.println(String.format("Desde %14s hasta %14s", date1, date2));
            System.out.println("------------------------------------------------------------------");
            if (intervals.size() == 0) {
                System.out.println("No hay localizaciones.");
            } else {
                List<LocationInterval> filteredIntervals = getFilteredIntervals(filterThreshold);
                for (LocationInterval interval : filteredIntervals) {
                    System.out.println(interval);
                }
            }
        } else {
            System.out.println("No existe el usuario en la base de datos.");
        }
        System.out.println("Valor de tiempo umbral: " + filterThreshold + " minutos.\n\n");
    }

    private List<LocationInterval> getFilteredIntervals(float filterThreshold) {
        List<LocationInterval> filteredIntervals = new ArrayList<>();
        if (intervals.size() > 0) {
            LocationInterval currentInterval = intervals.get(0).getCopy();
            filteredIntervals.add(currentInterval);
            int intervalIndex = 1;
            while (intervalIndex < intervals.size()) {
                boolean combine = intervals.get(intervalIndex).getDuration() < filterThreshold ||
                        intervals.get(intervalIndex).location.equals(currentInterval.location);
                if (combine) {
                    currentInterval.toDate = intervals.get(intervalIndex).toDate;
                } else {
                    currentInterval = intervals.get(intervalIndex).getCopy();
                    filteredIntervals.add(currentInterval);
                }
                intervalIndex++;
            }
        }

        return filteredIntervals;
    }

    private class LocationInterval implements Comparable<LocationInterval> {

        private String location;
        private DateTime fromDate;
        private DateTime toDate;

        private LocationInterval(String location, DateTime fromDate, DateTime toDate) {
            this.location = location;
            this.fromDate = fromDate;
            this.toDate = toDate;
        }

        // Returns the length of the interval in minutes.
        private float getDuration() {
            return (toDate.getMillis() - fromDate.getMillis()) / 60000;
        }

        private LocationInterval getCopy() {
            return new LocationInterval(location, fromDate, toDate);
        }

        @Override
        public int compareTo(LocationInterval other) {
            return fromDate.compareTo(other.fromDate);
        }

        @Override
        public String toString() {
            String date1 = fromDate.toString("dd/MM HH:mm:ss");
            String date2 = toDate.toString("dd/MM HH:mm:ss");
            return String.format("Desde %14s hasta %14s en: %20s", date1, date2, location);
        }
    }
}
