package com.example.sacms;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ActivityManagementApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(this.getClass().getResource("splashScreen.fxml"));
        Scene scene = new Scene(root);
        Image icon = new Image("file:src/main/resources/com/example/images/icon.png");
        stage.getIcons().add(icon);
        stage.setScene(scene);
        stage.setTitle("School Activity Club Management System ");
        stage.show();
        stage.setResizable(false);

    }

    public static void main(String[] args) {
        launch();
    }
}