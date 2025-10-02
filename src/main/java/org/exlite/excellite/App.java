package org.exlite.excellite;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("MainWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
        stage.setTitle("Excel at home");
        stage.setMinHeight(600);
        stage.setMinWidth(900);
        stage.show();
    }

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
