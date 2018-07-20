package es.uji.esansano.sliwquery;

import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.query.SliwQuery;
import org.elasticsearch.common.joda.time.DateTime;

import java.util.*;

public class WriteCSV {

    private static final int PORT = 9500;

    public static void main(String[] args) {

        // Get user data by name
        String userName = "arturo";
        SliwQuery controlQuery = new SliwQuery(PORT);
        Map<String, User> userMap = controlQuery.getUserMap();
        User user = userMap.get(userName);

        // Save validated data in csv format
        DateTime from = new DateTime(2018, 6, 7, 0, 1);
        DateTime to = new DateTime(2018, 7, 13, 23, 59);
        controlQuery.generateValidatedCSV(user, from, to);

        // Save training data in csv format
        controlQuery.generateLabeledTestCSV(user);
    }
}
