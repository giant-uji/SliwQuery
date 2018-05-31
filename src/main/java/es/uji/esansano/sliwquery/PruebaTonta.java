package es.uji.esansano.sliwquery;

import es.uji.esansano.sliwquery.models.Report;
import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.print.Output;
import es.uji.esansano.sliwquery.query.SliwQuery;
import org.elasticsearch.common.joda.time.DateTime;

import java.util.Map;

public class PruebaTonta {

    private static final DateTime FROM = new DateTime(2018, 5, 29, 18, 30);
    private static final DateTime TO = new DateTime(2018, 5, 31, 10, 0);

    private static final String[] USERS = new String[]{
      "montserrat barranquero", "arturo", "raul", "emilio"
    };

    public static void main(String[] args) {
        Map<String, User> userMap = SliwQuery.getUserMap();
        User user = userMap.get(USERS[0]);
        Report report = SliwQuery.getReport(user, FROM, TO, false);


        Output.printUsers(userMap.values());
        Output.printLocations(user, report, true);
        // Output.printReport(report, 0, null, null);
        Output.printReport(report, 5, null, null);
    }
}
