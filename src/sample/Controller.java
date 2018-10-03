package sample;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;

public class Controller {

    private static final String[] settingsFields = {"SQLServerField","SQLBDField","SQLUserField","SQLPasswordField"};

    @FXML
    private Button saveSettingsButton;

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

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
                TextField currentField = (TextField) (saveSettingsButton.getScene().lookup("#"+settingsName));
                currentField.setText(settings.get(settingsName));
            }
        }
    }

    public void recreateDB(ActionEvent actionEvent) {
        ConnectorSQL connectorSQL = ConnectorSQL.getDataDB();
        if (connectorSQL == null) {
            showError("Ошибка СУБД", "");
        } else {
            try {
                connectorSQL.createTable();
            } catch (Exception e) {
                showError("Ошибка СУБД", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void loadCsvToDB(ActionEvent actionEvent) {
        ConnectorSQL connectorSQL = ConnectorSQL.getDataDB();
        if (connectorSQL == null) {
            showError("Ошибка СУБД", "");
        } else {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("csv","*.csv"));
                File file = fileChooser.showOpenDialog(saveSettingsButton.getScene().getWindow());
                connectorSQL.loadCsvExchangeData(file);
            } catch (SQLException e) {
                showError("Ошибка СУБД", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void recalculateDB(ActionEvent actionEvent) {
        ConnectorSQL connectorSQL = ConnectorSQL.getDataDB();
        if (connectorSQL == null) {
            showError("Ошибка СУБД", "");
        } else {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("csv","*.csv"));
                File file = fileChooser.showOpenDialog(saveSettingsButton.getScene().getWindow());
                connectorSQL.loadCsvExchangeData(file);
            } catch (Exception e) {
                showError("Ошибка СУБД", e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
