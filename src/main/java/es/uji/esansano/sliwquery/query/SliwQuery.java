package es.uji.esansano.sliwquery.query;

import com.google.gson.*;
import es.uji.esansano.sliwquery.ml.MLServiceImpl;
import es.uji.esansano.sliwquery.models.*;
import es.uji.esansano.sliwquery.utils.Utils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.io.*;
import java.util.*;

public class SliwQuery {

    private int port;

    public SliwQuery(int port) {
        this.port = port;
    }


    public Map<String, User> getUserMap() {
        Map<String, User> nameUserMap = new HashMap<>();
        for (User user: getUsers()) {
            nameUserMap.put(user.getName(), user);
        }
        return nameUserMap;
    }


    public Report getReport(User user, DateTime from, DateTime to) {
        Report report = new Report();
        if (user != null) {
            List<Sample> samples = getSamples(user, from, to, false);
            report = new Report(user.getName(), from, to);
            report.setSamples(samples);
        }
        return report;
    }


    public List<Device> getDevices() {

        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("indoorlocplatform.uji.es", port));

        SearchResponse response = client.prepareSearch("sliw")
                .setTypes("devices")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(0).setSize(10000).setExplain(true)
                .execute()
                .actionGet();

        client.close();
        List<Device> devices = new ArrayList<>();
        JsonParser parser = new JsonParser();
        JsonObject responseJson = parser.parse(response.toString()).getAsJsonObject();
        JsonArray hitsArray = responseJson.getAsJsonObject("hits").getAsJsonArray("hits");

        Gson gson = new GsonBuilder().create();

        for (int i = 0; i < hitsArray.size(); i++) {
            JsonObject source = hitsArray.get(i).getAsJsonObject().getAsJsonObject("_source");
            Device device = gson.fromJson(source, Device.class);
            devices.add(device);
        }

        return devices;
    }


    private List<Sample> getSamples(User user, DateTime from, DateTime to, boolean valid) {
        List<Sample> samples = new ArrayList<Sample>();
        MLServiceImpl mlService = new MLServiceImpl();

        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("indoorlocplatform.uji.es", port));

        QueryBuilder qb = QueryBuilders.boolQuery()
                //.must(QueryBuilders.termQuery("valid", "false"))
                .must(QueryBuilders.matchQuery("userId", user.getId()));

        SearchResponse response = client.prepareSearch("sliw")
                .setTypes("samples")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(qb)
                .setPostFilter(FilterBuilders.rangeFilter("date").from(from.getMillis()).to(to.getMillis()))
                .setFrom(0).setSize(10000).setExplain(true)
                .execute()
                .actionGet();

        client.close();

        JsonParser parser = new JsonParser();
        JsonObject responseJson = parser.parse(response.toString()).getAsJsonObject();
        JsonArray hitsArray = responseJson.getAsJsonObject("hits").getAsJsonArray("hits");

        Gson gson = new GsonBuilder().create();

        for (int i = 0; i < hitsArray.size(); i++) {
            JsonObject source = hitsArray.get(i).getAsJsonObject().getAsJsonObject("_source");
            Sample sample = gson.fromJson(source, Sample.class);
            sample.setDate(new DateTime(source.get("date").getAsLong()));
            if (sample.isValid() == valid) {
                samples.add(sample);
            }
        }

        Collections.sort(samples);

        return samples;
    }


    private List<User> getUsers() {

        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("indoorlocplatform.uji.es", port));

        SearchResponse response = client.prepareSearch("sliw")
                .setTypes("users")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setFrom(0).setSize(10000).setExplain(true)
                .execute()
                .actionGet();

        client.close();
        List<User> users = new ArrayList<User>();
        JsonParser parser = new JsonParser();
        JsonObject responseJson = parser.parse(response.toString()).getAsJsonObject();
        JsonArray hitsArray = responseJson.getAsJsonObject("hits").getAsJsonArray("hits");

        Gson gson = new GsonBuilder().create();

        for (int i = 0; i < hitsArray.size(); i++) {
            JsonObject source = hitsArray.get(i).getAsJsonObject().getAsJsonObject("_source");
            User user = gson.fromJson(source, User.class);
            users.add(user);
        }

        return users;
    }

    private int writeCSV(User user, DateTime from, DateTime to, boolean valid, String label, String fileName) {

        // Samples that have been validated
        List<Sample> samples = getSamples(user, from, to, valid);
        String bssid = "";
        int observations = samples.size();

        // Columns of the csv file
        Map<String, int[]> features = new HashMap<>();

        // Labels of the csv file
        String[] labels = new String[observations];
        String[] clabels = new String[observations];
        int index = 0;

        StringJoiner csvHeader = new StringJoiner(",");
        StringJoiner[] rows = new StringJoiner[observations];
        List<String> bssids = null;

        if (valid) {
            // Extract BSSID, level and location from data
            for (Sample sample: samples) {
                rows[index] = new StringJoiner(",");
                for (Sample.WifiScanResult scan: sample.getScanResults()) {
                    bssid = scan.BSSID;
                    if (!features.containsKey(bssid)) {
                        features.put(bssid, new int[observations]);
                    }
                    features.get(bssid)[index] = scan.level;
                    labels[index] = sample.getLocation();
                }
                index++;
            }
            bssids = new ArrayList<>(features.keySet());
            Collections.sort(bssids);
        } else {
            bssids = getBSSIDList(user);
            for (String validatedBssid: bssids) {
                features.put(validatedBssid, new int[observations]);
            }
            // Extract level from data
            for (Sample sample: samples) {
                rows[index] = new StringJoiner(",");
                for (Sample.WifiScanResult scan: sample.getScanResults()) {
                    bssid = scan.BSSID;
                    if (features.containsKey(bssid)) {
                        features.get(bssid)[index] = scan.level;
                    }
                }
                clabels[index] = sample.getLocation();
                labels[index] = label;
                index++;
            }
        }

        // csv header and rows
        for (String bssidHeader: bssids) {
            csvHeader.add(bssidHeader);
            for (int i = 0; i < observations; i++) {
                rows[i].add(String.valueOf(features.get(bssidHeader)[i]));
            }
        }

        // Write csv file
        try {
            Writer writer = new FileWriter(fileName);
            csvHeader.add("label");
            if (!valid) {
                csvHeader.add("clabel");
            }
            writer.append(csvHeader.toString()).append("\n");
            for (int i = 0; i < observations; i++) {
                rows[i].add(labels[i]);
                if (!valid) {
                    rows[i].add(clabels[i]);
                }
                writer.append(rows[i].toString()).append("\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return observations;
    }

    public void generateValidatedCSV(User user, DateTime from, DateTime to) {
        String fileName = "data/" + user.getName() + "_validated.csv";
        int n = writeCSV(user, from, to, true, "", fileName);
        System.out.println("Writing csv file with validated data for user " + user.getName());
        System.out.println(n + " observations written.");
    }

    private int generateTrainingCSV(User user, DateTime from, DateTime to, String label) {
        String fileName = "data/" + user.getName() + "_" + String.valueOf(from.getMillis()) +
                "_" + String.valueOf(to.getMillis()) + "_" + label.toLowerCase() + ".csv";
        return writeCSV(user, from, to, false, label, fileName);
    }

    private List<String> getBSSIDList(User user) {
        String fileName = "data/" + user.getName() + "_validated.csv";
        try {
            BufferedReader csvFile = new BufferedReader(new FileReader(fileName));
            ArrayList<String> bssids = new ArrayList<>(Arrays.asList(csvFile.readLine().split(",")));
            bssids.remove(bssids.size() - 1); // Remove "label"
            return bssids;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void generateTrainingCSV(User user) {
        List<Interval> intervals = Utils.getIntervals(user.getName());
        for (Interval interval: intervals) {
            int n = generateTrainingCSV(user, interval.getFromDate(), interval.getToDate(), interval.getLocation());
            System.out.println("Writing csv file with labelled data for user " + user.getName());
            System.out.println(interval);
            System.out.println(n + " observations written.");
        }
    }
}
