package es.uji.esansano.sliwquery;

import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.query.SliwQuery;
import org.elasticsearch.common.joda.time.DateTime;

import java.util.*;

public class WriteCSV {

    private static final int PORT = 9500;

    public static void main(String[] args) {

        // Get user data by name
        String userName = "emilio";
        SliwQuery controlQuery = new SliwQuery(PORT);
        Map<String, User> userMap = controlQuery.getUserMap();
        User user = userMap.get(userName);

        // Save validated data in csv format
        DateTime from = new DateTime(2018, 6, 1, 21, 0);
        DateTime to = new DateTime(2018, 6, 4, 10, 0);
        controlQuery.generateValidatedCSV(user, from, to);

        // Save training data in csv format
        controlQuery.generateTrainingCSV(user);
    }
}
