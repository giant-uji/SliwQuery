package es.uji.esansano.sliwquery.models;

import org.elasticsearch.common.joda.time.DateTime;

public class Period {

    private DateTime from;
    private DateTime to;
    private String label;

    public Period(DateTime from, DateTime to, String label) {
        this.from = from;
        this.to = to;
        this.label = label;
    }

    public DateTime getFrom() {
        return from;
    }

    public DateTime getTo() {
        return to;
    }

    public String getLabel() {
        return label;
    }
}
