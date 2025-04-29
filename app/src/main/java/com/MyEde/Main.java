package com.MyEde;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Messenger messenger = Messenger.getInstance();
        MainController controller = new MainController();


        controller.setPrimaryStage(stage);
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("controlPanel.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 750);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/MyEde/style.css")).toExternalForm());
        messenger.setMainController(fxmlLoader.getController());
        stage.setTitle("EDE App");

        stage.setScene(scene);
        stage.show();


    }

    public static void main(String[] args) {
        launch();
    }
}