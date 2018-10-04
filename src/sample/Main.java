package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("resources/fxmls/sample.fxml"));
        primaryStage.setTitle("FXNN");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        try {
            ConnectorSQL.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainNeuralNetwork.init();
        launch(args);
    }
}
