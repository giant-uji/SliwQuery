package es.uji.esansano.sliwquery;

import es.uji.esansano.sliwquery.models.Report;
import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.utils.Utils;
import es.uji.esansano.sliwquery.query.SliwQuery;
import org.elasticsearch.common.joda.time.DateTime;

import java.util.Map;

public class PruebaTonta {

    private static final DateTime FROM = new DateTime(2018, 6, 1, 21, 0);
    private static final DateTime TO = new DateTime(2018, 6, 4, 10, 0);

    private static final String[] USERS = new String[]{
      "arturo", "raul", "emilio", "oscar"
    };

    public static void main(String[] args) {

        SliwQuery seniorQuery = new SliwQuery(9300);
        SliwQuery controlQuery = new SliwQuery(9500);

        Map<String, User> userMap = controlQuery.getUserMap();
        User user = userMap.get(USERS[2]);
        Report report = controlQuery.getReport(user, FROM, TO);


        Utils.printUsers(userMap.values());
        Utils.printLocations(user, report, true);
        // Utils.printReport(report, 0, null, null);
        Utils.printReport(report, 5, null, null);
    }
}
