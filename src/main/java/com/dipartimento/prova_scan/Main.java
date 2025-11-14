package com.dipartimento.prova_scan;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/main.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Gestione Scadenze Prodotti");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

