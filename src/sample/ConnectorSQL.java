package sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            Files.lines(file.toPath()).forEach(line -> {
                if(sql.length() != 0) {
                    sql.append("\n");
                    sqlDelete.append("\n");
                }
                sql.append("INSERT INTO EXCHANGE_DATA (").append(ORDER_IN_CSV).append( ") VALUES (");
                String[] parts = line.split(",");
                boolean first = true;
                for (String csvField: parts) {
                    //first is date
                    if (!first) {
                        sql.append(",");
                    } else {
                        sqlDelete.append("DELETE FROM EXCHANGE_DATA as T1 where T1._date ='"+csvField+"';");
                    }
                    sql.append("'").append(csvField).append("'");
                    first = false;
                }
                sql.append(");");
            });
            connection.createStatement().execute(sqlDelete.toString());
            connection.createStatement().execute(sql.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void recalculateDB() throws SQLException {
        StringBuilder sql = new StringBuilder();
        ResultSet resultSet = connection.createStatement().executeQuery("select T1._date, T1._open, T1._high, T1._low, T1._close from exchange_data as T1 order by T1._date");
        while (resultSet.next()) {
            double high = resultSet.getDouble("_high");
            double low = resultSet.getDouble("_low");
            double open = resultSet.getDouble("_open");
            double close = resultSet.getDouble("_close");
            double range = high - low;
            if (range == 0) {
                range = 0.00001;
            }
            double top_tail = (high-Math.max(open, close))/range;
            double bottom_tail = (Math.min(open, close)-low)/range;
            double body = close - open;
            double bodyPos, bodyNeg;
            if (body > 0) {
                bodyPos = body/range;
                bodyNeg = 0;
            } else {
                bodyPos = 0;
                bodyNeg = body/range;
            }

            if (sql.length() != 0) {
                sql.append("\n");
            }
            //sql.append("update")
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
                "_body real," +             //ut, gt, b+, b- ratio
                "_top_tail real," +         //
                "_bottom_tail real," +      //one of 6 relative value and ma
                "_ma20 real," +             //mom~, stac~ 6 position of 2 graph
                "_ma80 real," +             //Encog?
                "_is_up boolean," +
                "_is_down boolean," +
                "_is_middle boolean," +
                "PRIMARY KEY (_date))";
    }
}
