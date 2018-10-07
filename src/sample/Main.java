package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("resources/fxmls/sample.fxml"));
        primaryStage.setTitle("FXNN");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                MainNeuralNetwork.getANN().save(new File(MainNeuralNetwork.PATH_TO_SAVE_ANN));
            }
        });
    }


    public static void main(String[] args) {
        try {
            ConnectorSQL.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainNeuralNetwork.init();
        File file = new File(MainNeuralNetwork.PATH_TO_SAVE_ANN);
        if (file.exists()) {
            try {
                MainNeuralNetwork.getANN().load(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        launch(args);
    }
}
