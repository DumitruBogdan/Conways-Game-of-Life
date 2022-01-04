import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public void start (Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("gameOfLife.fxml"));
        primaryStage.setTitle("Welcome to the Game of Life!");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}

