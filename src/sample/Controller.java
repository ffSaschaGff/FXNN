package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.util.HashMap;

public class Controller {

    private static final String[] settingsFields = {"SQLServerField","SQLBDField","SQLUserField","SQLPasswordField"};

    @FXML
    private Button SaveSettingsButton;

    public void saveSettings(ActionEvent actionEvent) {
        HashMap<String, String> settings = new HashMap<String, String>();
        for(String settingsField: Controller.settingsFields) {
            TextField currentField = (TextField) ((Button) actionEvent.getSource()).getScene().lookup("#"+settingsField);
            settings.put(settingsField,currentField.getText());
        }
        SettingsKeepper.saveSettings(settings);
    }

    public void propertyTabSelected(Event event) {
        HashMap<String, String> settings = SettingsKeepper.loadSettings();
        for(String settingsName: Controller.settingsFields) {
            if(settings.containsKey(settingsName)) {
                TextField currentField = (TextField) (SaveSettingsButton.getScene().lookup("#"+settingsName));
                currentField.setText(settings.get(settingsName));
            }
        }
    }

    public void recreateDB(ActionEvent actionEvent) {
        ConnectorSQL connectorSQL = ConnectorSQL.getDataDB();
        if (connectorSQL == null) {
            try {
                ConnectorSQL.init();
                connectorSQL = ConnectorSQL.getDataDB();
                connectorSQL.createTable();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка СУБД");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
                e.printStackTrace();
            }
        }
    }

    public void loadCsvToDB(ActionEvent actionEvent) {
    }
}
