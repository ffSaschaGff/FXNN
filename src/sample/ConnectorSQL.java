package sample;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class ConnectorSQL {

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

    private ConnectorSQL() throws Exception {
        HashMap<String, String> properties = SettingsKeepper.loadSettings();
        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://"+properties.get("SQLServerField")+"/"+properties.get("SQLBDField");
        connection = DriverManager.getConnection(url,properties.get("SQLUserField"), properties.get("SQLPasswordField"));

    }

    private String getCreateTableRequest() {
        return "DROP TABLE IF EXISTS FX_DATA;" +
                "CREATE TABLE FX_DATA (" +
                "_date timestamp NOT NULL," +
                "_open real," +
                "_high real," +
                "_low real," +
                "_close real" +
                ")";
    }
}
