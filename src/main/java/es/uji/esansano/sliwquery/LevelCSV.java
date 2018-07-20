package es.uji.esansano.sliwquery;

import es.uji.esansano.sliwquery.models.Report;
import es.uji.esansano.sliwquery.models.Sample;
import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.query.SliwQuery;
import es.uji.esansano.sliwquery.utils.Utils;
import org.elasticsearch.common.joda.time.DateTime;

import java.util.List;
import java.util.Map;

public class LevelCSV {
    private static final DateTime FROM = new DateTime(2018, 4, 29, 1, 0);
    private static final DateTime TO = new DateTime(2018, 7, 30, 23, 0);

    /*private static final String[] USERS = new String[]{
            "arturo", "raul", "emilio", "oscar"
    };*/


    public static void main(String[] args) {
        int counter = 0;
        SliwQuery seniorQuery = new SliwQuery(9300);

        Map<String, User> userMap = seniorQuery.getUserMap();
        //for (Map.Entry<String, User> entry : userMap.entrySet()) {
            //User user = userMap.get(entry.getValue().getName());
            User user = userMap.get("rosa maria saenz");
            List<Sample> samples = seniorQuery.getSamples(user, FROM, TO, false);
            System.out.println(user.getName() + "\n-----------------\n");
            for (Sample sample : samples) {
                for (Sample.WifiScanResult scanResult : sample.getScanResults()) {
                    System.out.println(scanResult.SSID + " " + scanResult.level + " " + sample.getDate().getDayOfMonth() + "-" + sample.getDate().getMonthOfYear() + "-" + sample.getDate().getYear());
                }
                counter++;if(counter > 10) break;
            }
            System.out.println("\n\n\n");


        //}

        //User user = userMap.get(USERS[2]);
        //Report report = seniorQuery.getReport(user, FROM, TO);


        /*Utils.printUsers(userMap.values());
        Utils.printLocations(user, report, true);
        // Utils.printReport(report, 0, null, null);
        Utils.printReport(report, 5, null, null);*/
    }
}
