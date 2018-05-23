package es.uji.esansano.sliwquery.models;

import org.elasticsearch.common.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class Sample implements Comparable<Sample> {

    public static class WifiScanResult {

        public String SSID;
        public String BSSID;
        public int level;

        public WifiScanResult() {
            super();
        }

        public WifiScanResult(String SSID, String BSSID, int level) {
            this.SSID = SSID;
            this.BSSID = BSSID;
            this.level = level;
        }

        @Override
        public String toString() {
            return "WifiScanResult{" +
                    "SSID='" + SSID + '\'' +
                    ", BSSID='" + BSSID + '\'' +
                    ", level=" + level +
                    '}';
        }

    }

    private String id;
    private String userId;
    private String deviceId;
    private String location;
    private transient DateTime date;
    private List<WifiScanResult> scanResults;
    private boolean valid;

    public Sample() {
        super();
        this.date = new DateTime();
        this.scanResults = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public List<WifiScanResult> getScanResults() {
        return scanResults;
    }

    public void setScanResults(List<WifiScanResult> scanResults) {
        this.scanResults = scanResults;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int compareTo(Sample otherSample) {
        return date.compareTo(otherSample.date);
    }

    @Override
    public String toString() {
        return "Sample{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", location='" + location + '\'' +
                ", date=" + date +
                ", scanResults=" + scanResults +
                ", valid=" + valid +
                '}';
    }

}
