package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class ConnectorSQL {

    private static final String ORDER_IN_CSV = "_date, _open, _high, _low, _close";

    private static ConnectorSQL dataDB;

    private Connection connection;

    public static void init() throws Exception {
        ConnectorSQL.dataDB = new ConnectorSQL();
    }

    public static ConnectorSQL getDataDB() {
        return ConnectorSQL.dataDB;
    }

    public void createTable() throws SQLException {
        connection.createStatement().execute(getCreateTableRequest());
    }

    public void loadCsvExchangeData(File file) throws SQLException {
        StringBuilder sqlDelete = new StringBuilder();
        StringBuilder sql = new StringBuilder();
        try {
            for (String line: (new String(Files.readAllBytes(file.toPath()))).split("\n")) {
                if (line.length()==0) {
                    continue;
                }
                line = line.replaceAll("��","");
                line = line.replaceAll("\u0000", "");
                if(sql.length() != 0) {
                    sql.append("\n");
                    sqlDelete.append("\n");
                }
                sql.append("INSERT INTO EXCHANGE_DATA (").append(ORDER_IN_CSV).append( ") VALUES (");
                String[] parts = line.split(",");
                boolean first = true;
                int iterator = 0;
                for (String csvField: parts) {
                    if (iterator > 4) {
                        break;
                    }
                    //first is date
                    if (!first) {
                        sql.append(",");
                    } else {
                        sqlDelete.append("DELETE FROM EXCHANGE_DATA as T1 where T1._date ='"+csvField+"';");
                    }
                    sql.append("'").append(csvField).append("'");
                    first = false;
                    iterator++;
                }
                sql.append(");");
            }
            connection.createStatement().execute(sqlDelete.toString());
            connection.createStatement().execute(sql.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recalculateDB() throws SQLException {
        StringBuilder sql = new StringBuilder();
        ResultSet resultSet = connection.createStatement().executeQuery("select T1._date, T1._open, T1._high, T1._low, T1._close from exchange_data as T1 order by T1._date");
        ArrayList<PreperedDataRow> dataRows = PreperedDataRow.generateArrayOfData(resultSet);
        for (PreperedDataRow row: dataRows) {
            //if (row.isFullData()) {
                if (sql.length() != 0) {
                    sql.append("\n");
                }
                sql.append("UPDATE EXCHANGE_DATA as T1 set ");
                sql.append("_body_pos = '").append(row.getBodyPos()).append("', ");
                sql.append("_body_neg = '").append(row.getBodyNeg()).append("', ");
                sql.append("_ma1 = '").append(row.getMa1()).append("', ");
                sql.append("_ma2 = '").append(row.getMa2()).append("', ");
                sql.append("_top_tail = '").append(row.getTopTail()).append("', ");
                sql.append("_bottom_tail = '").append(row.getBottomTail()).append("', ");
                sql.append("_is_up = '").append(row.isFutureUp()).append("', ");
                sql.append("_is_down = '").append(row.isFutureDown()).append("', ");
                sql.append("_is_middle = '").append(row.isFutureSame()).append("', ");
                sql.append("_tm1m2 = '").append(row.isTm1m2()).append("', ");
                sql.append("_tm2m1 = '").append(row.isTm2m1()).append("', ");
                sql.append("_m1tm2 = '").append(row.isM1tm2()).append("', ");
                sql.append("_m1m2t = '").append(row.isM1m2t()).append("', ");
                sql.append("_m2m1t = '").append(row.isM2m1t()).append("', ");
                sql.append("_m2tm1 = '").append(row.isM2tm1()).append("' ");
                sql.append(" where T1._date = '").append(row.getDate()).append("';");
            //}
        }
        connection.createStatement().execute(sql.toString());
    }

    public ResultSet getDataForTraining() throws SQLException {
        return connection.createStatement().executeQuery("select * from exchange_data as t1 where t1._is_up or t1._is_down or t1._is_middle order by T1._date;");
    }

    public ResultSet getLastData(String date) throws SQLException {
        if (date.length()==0) {
            return connection.createStatement().executeQuery("select * from exchange_data as t2 where t2._date in (select T1._date from exchange_data as T1 order by T1._date desc limit 5) order by t2._date");
        } else {
            return connection.createStatement().executeQuery("select * from exchange_data as t2 where t2._date in (select T1._date from exchange_data as T1 where T1._date <= '"+date+"' order by T1._date desc limit 5) order by t2._date");
        }
    }

    private ConnectorSQL() throws Exception {
        HashMap<String, String> properties = SettingsKeepper.loadSettings();
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://"+properties.get("SQLServerField")+"/"+properties.get("SQLBDField");
        connection = DriverManager.getConnection(url,properties.get("SQLUserField"), properties.get("SQLPasswordField"));

    }

    private String getCreateTableRequest() {
        return "DROP TABLE IF EXISTS EXCHANGE_DATA;" +
                "CREATE TABLE EXCHANGE_DATA (" +
                "_date timestamp NOT NULL," +
                "_open real," +
                "_high real," +
                "_low real," +
                "_close real," +
                "_body_pos real," +
                "_body_neg real," +             //ut, gt, b+, b- ratio
                "_top_tail real," +         //
                "_bottom_tail real," +      //one of 6 relative value and ma
                "_ma1 real," +             //mom~, stac~ 6 position of 2 graph
                "_ma2 real," +             //Encog?
                "_is_up boolean," +
                "_is_down boolean," +
                "_is_middle boolean," +
                "_tm1m2 boolean," +
                "_tm2m1 boolean," +
                "_m1tm2 boolean," +
                "_m1m2t boolean," +
                "_m2m1t boolean," +
                "_m2tm1 boolean," +
                "PRIMARY KEY (_date))";
    }
}
