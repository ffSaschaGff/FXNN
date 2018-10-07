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
    @FXML
    private TextField dateOfResult;
    @FXML
    private TextField requiredError;

    public void showError(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
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
            showError("Ошибка СУБД", "", Alert.AlertType.ERROR);
        } else {
            try {
                connectorSQL.createTable();
            } catch (Exception e) {
                showError("Ошибка СУБД", e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public void loadCsvToDB(ActionEvent actionEvent) {
        ConnectorSQL connectorSQL = ConnectorSQL.getDataDB();
        if (connectorSQL == null) {
            showError("Ошибка СУБД", "", Alert.AlertType.ERROR);
        } else {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("csv","*.csv"));
                File file = fileChooser.showOpenDialog(saveSettingsButton.getScene().getWindow());
                connectorSQL.loadCsvExchangeData(file);
            } catch (SQLException e) {
                showError("Ошибка СУБД", e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public void recalculateDB(ActionEvent actionEvent) {
        ConnectorSQL connectorSQL = ConnectorSQL.getDataDB();
        if (connectorSQL == null) {
            showError("Ошибка СУБД", "", Alert.AlertType.ERROR);
        } else {
            try {
                connectorSQL.recalculateDB();
            } catch (Exception e) {
                showError("Ошибка СУБД", e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    public void trainingButtonClick(ActionEvent actionEvent) {
        int error = 10;
        try {
            error = Integer.parseInt(requiredError.getText());
        } catch (Exception e) {
            showError("Ошибка", "Не удалось распознать число, ошибка будет считаться до е-10", Alert.AlertType.ERROR);
        }
        try {
            MainNeuralNetwork.getANN().train(error, new CommonCallback<Boolean, Double>() {
                @Override
                public Boolean call(Double event) {
                    showError("Ошибка бучения: ", event.toString(), Alert.AlertType.INFORMATION);
                    return false;
                }
            });
        } catch (SQLException e) {
            showError("Ошибка СУБД", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public void createNewANN(ActionEvent actionEvent) {
        MainNeuralNetwork.init();
    }

    public void loadANN(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("ANN","*.ann"));
        File file = fileChooser.showOpenDialog(saveSettingsButton.getScene().getWindow());
        MainNeuralNetwork.getANN().load(file);
    }

    public void saveANN(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("ANN","*.ann"));
        File file = fileChooser.showSaveDialog(saveSettingsButton.getScene().getWindow());
        MainNeuralNetwork.getANN().save(file);
    }

    public void calculateByLastData(ActionEvent actionEvent) {
        try {
            MainNeuralNetwork.getANN().getResultByLastData(this.dateOfResult.getText(),new CommonCallback<Boolean, double[]>() {
                @Override
                public Boolean call(double[] event) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i = 0; i < Math.min(event.length, MainNeuralNetwork.OUTPUT_NAMES.length); i++) {
                        if(stringBuilder.length() != 0) {
                            stringBuilder.append("\n");
                        }
                        stringBuilder.append(MainNeuralNetwork.OUTPUT_NAMES[i]).append(": ").append(event[i]);
                    }
                    showError("Результат", stringBuilder.toString(), Alert.AlertType.INFORMATION);
                    return false;
                }
            });
        } catch (SQLException e) {
            showError("Ошибка СУБД", e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }
}
