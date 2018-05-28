package es.uji.esansano.sliwquery.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import es.uji.esansano.sliwquery.models.Report;
import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.print.Output;
import es.uji.esansano.sliwquery.query.SliwQuery;
import org.elasticsearch.common.joda.time.DateTime;

import javax.swing.*;
import java.awt.event.*;
import java.util.Map;

public class App {
    private JPanel panelMain;

    private JTextField thresholdField;
    private JTextField intervalField;
    private JButton buttonCancel;
    private JButton buttonReport;
    private JComboBox usersBox;
    private JLabel usersLabel;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JLabel thresholdLabel;
    private JLabel intervalLabel;
    private JTextArea resultArea;
    private DatePicker fromDatePicker;
    private DatePicker toDatePicker;
    private TimePicker fromTimePicker;
    private TimePicker toTimePicker;

    private Map<String, User> userMap;
    private String userName = "";

    public App() {
        usersLabel.setText("Users:");
        fromLabel.setText("From:");

        userMap = SliwQuery.getUserMap();
        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            usersBox.addItem(entry.getKey());
        }

        thresholdField.setText("5");
        intervalField.setText("10");

        usersBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                userName = (String) itemEvent.getItem();
            }
        });
        buttonReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                makeReport();
            }
        });

        //resultArea = new JTextArea();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("App");
        frame.setContentPane(new App().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void makeReport() {
        int dayFROM = fromDatePicker.getDate().getDayOfMonth();
        int monthFROM = fromDatePicker.getDate().getMonthValue();
        int yearFROM = fromDatePicker.getDate().getYear();
        int fromHour = fromTimePicker.getTime().getHour();
        int fromMin = fromTimePicker.getTime().getMinute();

        int dayTO = toDatePicker.getDate().getDayOfMonth();
        int monthTO = toDatePicker.getDate().getMonthValue();
        int yearTO = toDatePicker.getDate().getYear();
        int toHour = toTimePicker.getTime().getHour();
        int toMin = toTimePicker.getTime().getMinute();

        DateTime FROM = new DateTime(yearFROM, monthFROM, dayFROM, fromHour, fromMin);
        DateTime TO = new DateTime(yearTO, monthTO, dayTO, toHour, toMin);

        User user = userMap.get(userName);
        System.out.println(user);
        Report report = SliwQuery.getReport(user, FROM, TO, false);
        report.setUNKNOWN_INTERVAL(Integer.valueOf(intervalField.getText()));
        int threshhold = Integer.valueOf(thresholdField.getText());

        String reportResult = Output.getReport(report, threshhold, null, null);

        resultArea.setText(reportResult);
    }

}
