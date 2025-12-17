package com.dipartimento.expiryManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 600);
        String css = this.getClass().getResource("/css/style.css").toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("Gestione Scadenze Prodotti");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}