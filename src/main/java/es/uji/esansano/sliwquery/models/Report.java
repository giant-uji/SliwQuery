package es.uji.esansano.sliwquery.models;

import es.uji.esansano.sliwquery.ml.MLServiceImpl;
import org.elasticsearch.common.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Report {

    private float UNKNOWN_INTERVAL = 10;
    private String user;
    private DateTime fromDate;
    private DateTime toDate;
    private List<LocationInterval> intervals = new ArrayList<>();
    private List<Sample> samples;

    private int samplesWithScan;
    private long timeFirstSample;
    private long timeLastSample;

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
            timeFirstSample = samples.get(0).getDate().getMillis();
            int firstValidSample = 0;
            while (firstValidSample < samples.size() && samples.get(firstValidSample).getScanResults().size() == 0) {
                firstValidSample++;
            }

            if (firstValidSample < samples.size()) {
                samplesWithScan++;
                String currentLocation = samples.get(firstValidSample).getLocation();
                DateTime startDate = samples.get(firstValidSample).getDate();
                DateTime prevDate = startDate;

                int sampleIndex = firstValidSample + 1;
                boolean closeInterval;
                Sample currentSample = null;

                while (sampleIndex < samples.size()) {
                    currentSample = samples.get(sampleIndex);
                    if (samples.get(sampleIndex).getScanResults().size() > 0) {
                        samplesWithScan++;
                        float timeInterval = (currentSample.getDate().getMillis() - prevDate.getMillis()) / 60000f;
                        closeInterval = !currentSample.getLocation().equals(currentLocation) ||
                                sampleIndex == samples.size() - 1 ||
                                timeInterval > UNKNOWN_INTERVAL;
                        if (closeInterval) {
                            if (timeInterval > UNKNOWN_INTERVAL) {
                                addInterval(currentLocation, startDate, prevDate);
                                currentLocation = MLServiceImpl.NO_DATA_AVAILABLE;
                                startDate = prevDate;
                            } else {
                                addInterval(currentLocation, startDate, currentSample.getDate());
                                currentLocation = currentSample.getLocation();
                                startDate = currentSample.getDate();
                            }

                        }
                    }
                    prevDate = currentSample.getDate();
                    timeLastSample = currentSample.getDate().getMillis();

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
            System.out.println(String.format("Valor de tiempo umbral: %f minutos.", filterThreshold));
            System.out.println("------------------------------------------------------------------");
            if (intervals.size() == 0) {
                System.out.println("No hay localizaciones.");
            } else {
                List<LocationInterval> filteredIntervals = getFilteredIntervals(filterThreshold);
                for (LocationInterval interval : filteredIntervals) {
                    System.out.println(interval);
                }
            }
            printStats();
        } else {
            System.out.println("No existe el usuario en la base de datos.");
        }
    }

    public String getReport(float filterThreshold, DateTime from, DateTime to) {
        String temp = "";
        if (user != null) {
            temp += (String.format("\n\nInforme para el usuario: %s", user)) + "\n";
            if (from == null) from = fromDate;
            if (to == null) to = toDate;
            String date1 = from.toString("dd/MM HH:mm:ss");
            String date2 = to.toString("dd/MM HH:mm:ss");
            temp += (String.format("Desde %14s hasta %14s", date1, date2)) + "\n";
            temp += (String.format("Valor de tiempo umbral: %f minutos.", filterThreshold)) + "\n";
            temp += ("------------------------------------------------------------------") + "\n";
            if (intervals.size() == 0) {
                temp += ("No hay localizaciones.") + "\n";
            } else {
                List<LocationInterval> filteredIntervals = getFilteredIntervals(filterThreshold);
                for (LocationInterval interval : filteredIntervals) {
                    temp += (interval) + "\n";
                }
            }
            temp += getStats() + "\n";
        } else {
            temp += ("No existe el usuario en la base de datos.") +"\n";
        }

        return  temp;
    }

    private void printStats() {
        System.out.println();
        float period = ((timeLastSample - timeFirstSample) / 3600000f);
        float length = ((toDate.getMillis() - fromDate.getMillis()) / 3600000f);
        System.out.println(String.format("%-40s%2.2f%s", "Report period length: ", length, " hours"));
        System.out.println(String.format("%-40s%d", "Number of samples: ", samples.size()));
        System.out.println(String.format("%-40s%d (%3.2f%%)", "Number of samples with scan data: ", samplesWithScan, (100f * samplesWithScan) / samples.size()));
        System.out.println(String.format("%-40s%2.2f", "Average number of samples per hour: ", samples.size() / period));

    }

    private String getStats() {
        String temp = "\n";
        float period = ((timeLastSample - timeFirstSample) / 3600000f);
        float length = ((toDate.getMillis() - fromDate.getMillis()) / 3600000f);
        temp += (String.format("%-40s%2.2f%s", "Report period length: ", length, " hours")) + "\n";
        temp += (String.format("%-40s%d", "Number of samples: ", samples.size())) + "\n";
        temp += (String.format("%-40s%d (%3.2f%%)", "Number of samples with scan data: ", samplesWithScan, (100f * samplesWithScan) / samples.size())) + "\n";
        temp += (String.format("%-40s%2.2f", "Average number of samples per hour: ", samples.size() / period))+ "\n";

        return temp;
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
                if (combine && !currentInterval.location.equals(MLServiceImpl.UNKNOWN_LOCATION)) {
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
            return String.format("Desde %14s hasta %14s en: %-25s", date1, date2, location);
        }
    }

    public void setUNKNOWN_INTERVAL(int value) {
        UNKNOWN_INTERVAL = value;
    }
}
