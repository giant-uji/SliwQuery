package es.uji.esansano.sliwquery.models;

import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.DateTimeFormat;
import org.elasticsearch.common.joda.time.format.DateTimeFormatter;

public class Interval implements Comparable<Interval> {

    private String location;
    private DateTime fromDate;
    private DateTime toDate;

    public Interval(DateTime fromDate, DateTime toDate, String location) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.location = location;
    }

    public Interval(String fromDateStr, String toDateStr, String location) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
        this.fromDate = formatter.parseDateTime(fromDateStr);
        this.toDate = formatter.parseDateTime(toDateStr);
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    public DateTime getFromDate() {
        return fromDate;
    }

    public DateTime getToDate() {
        return toDate;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setFromDate(DateTime fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(DateTime toDate) {
        this.toDate = toDate;
    }

    // Returns the length of the interval in minutes.
    public float getDuration() {
        return (toDate.getMillis() - fromDate.getMillis()) / 60000;
    }

    public Interval getCopy() {
        return new Interval(fromDate, toDate, location);
    }

    @Override
    public int compareTo(Interval other) {
        return fromDate.compareTo(other.fromDate);
    }

    @Override
    public String toString() {
        String date1 = fromDate.toString("dd/MM HH:mm:ss");
        String date2 = toDate.toString("dd/MM HH:mm:ss");
        return String.format("Desde %14s hasta %14s en: %-25s", date1, date2, location);
    }
}
