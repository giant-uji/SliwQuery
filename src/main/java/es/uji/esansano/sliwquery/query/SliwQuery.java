package es.uji.esansano.sliwquery.query;

import com.google.gson.*;
import es.uji.esansano.sliwquery.ml.MLServiceImpl;
import es.uji.esansano.sliwquery.models.Device;
import es.uji.esansano.sliwquery.models.Report;
import es.uji.esansano.sliwquery.models.Sample;
import es.uji.esansano.sliwquery.models.User;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.*;

public class SliwQuery {


    public static Map<String, User> getUserMap() {
        Map<String, User> nameUserMap = new HashMap<>();
        for (User user: SliwQuery.getUsers()) {
            nameUserMap.put(user.getName(), user);
        }
        return nameUserMap;
    }


    public static Report getReport(User user, DateTime from, DateTime to, boolean local) {
        Report report = new Report();
        if (user != null) {
            List<Sample> samples = getSamples(user, from, to, local);
            report.setSamples(samples);
            report = new Report(user.getName(), from, to);
        }
        return report;
    }


    public static List<Device> getDevices() {

        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("indoorlocplatform.uji.es", 9300));

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


    private static List<Sample> getSamples(User user, DateTime from, DateTime to, boolean local) {
        List<Sample> samples = new ArrayList<Sample>();
        MLServiceImpl mlService = new MLServiceImpl();

        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("indoorlocplatform.uji.es", 9300));

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
            if (local) {
                sample.setLocation(mlService.getLocalPrediction(user, sample, false));
            }
            samples.add(sample);
        }

        Collections.sort(samples);

        return samples;
    }


    private static List<User> getUsers() {

        Client client = new TransportClient()
                .addTransportAddress(new InetSocketTransportAddress("indoorlocplatform.uji.es", 9300));

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
}
