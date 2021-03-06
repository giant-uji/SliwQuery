package es.uji.esansano.sliwquery.view;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import es.uji.esansano.sliwquery.models.Report;
import es.uji.esansano.sliwquery.models.User;
import es.uji.esansano.sliwquery.utils.Utils;
import es.uji.esansano.sliwquery.query.SliwQuery;
import org.elasticsearch.common.joda.time.DateTime;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class App {
    private static final SliwQuery seniorQuery = new SliwQuery(9300);
    private static final SliwQuery controlQuery = new SliwQuery(9500);

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
    private JButton buttonCopy;
    private JRadioButton seniorMonitoringRadioButton;
    private JRadioButton controlMonitoringRadioButton;
    private JButton buttonAll;

    private Map<String, User> userMap;
    private String userName = "";
    private SliwQuery activeQuery;

    public App() {

        activeQuery = seniorQuery;
        createList();
        setDefaultValues();

        usersBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                userName = (String) itemEvent.getItem();
            }
        });
        buttonReport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                makeReport(activeQuery);
            }
        });
        buttonAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                makeAllReports(activeQuery);
            }
        });
        buttonCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String result = resultArea.getText();
                StringSelection stringSelection = new StringSelection(result);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setDefaultValues();
            }
        });
        seniorMonitoringRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeQuery = seniorQuery;
                usersBox.removeAllItems();
                resultArea.setText("");
                createList();
            }
        });
        controlMonitoringRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeQuery = controlQuery;
                usersBox.removeAllItems();
                resultArea.setText("");
                createList();
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Senior Monitoring - Generador de informes");
        frame.setContentPane(new App().panelMain);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    public void createList() {
        userMap = activeQuery.getUserMap();
        for (Map.Entry<String, User> entry : userMap.entrySet()) {
            String [] splitKey = entry.getKey().split(" ");
            if (!splitKey[0].equals("borrar") && !splitKey[0].equals("test1") && !splitKey[0].equals("prueba1"))
                usersBox.addItem(entry.getKey());
        }
    }

    public void makeReport(SliwQuery query) {
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
        Report report = query.getReport(user, FROM, TO);
        report.setUNKNOWN_INTERVAL(Integer.valueOf(intervalField.getText()));
        int threshhold = Integer.valueOf(thresholdField.getText());

        String reportResult = Utils.getReport(report, threshhold, null, null);

        resultArea.setText(reportResult);
    }

    public void makeAllReports(SliwQuery query) {
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

        resultArea.setText("");

         for (Map.Entry entry : userMap.entrySet()) {
             User user = userMap.get(entry.getKey());
             Report report = query.getReport(user, FROM, TO);
             report.setUNKNOWN_INTERVAL(Integer.valueOf(intervalField.getText()));
             int threshhold = Integer.valueOf(thresholdField.getText());

             String reportResult = Utils.getReport(report, threshhold, null, null);

             if (resultArea.getText().length() == 0)
                 resultArea.setText(reportResult);
             else
                 resultArea.setText(resultArea.getText() + reportResult);
        }
    }

    public void setDefaultValues() {
        resultArea.setText("");
        thresholdField.setText("5");
        intervalField.setText("10");
        fromDatePicker.setDate(LocalDate.now().with(DayOfWeek.MONDAY));
        fromTimePicker.setTime(LocalTime.MIDNIGHT);
        toDatePicker.setDate(LocalDate.now().with(DayOfWeek.SUNDAY));
        toTimePicker.setTime(LocalTime.MIDNIGHT);
    }
}
