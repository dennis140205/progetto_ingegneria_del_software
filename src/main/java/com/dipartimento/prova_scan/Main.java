package com.dipartimento.prova_scan;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/main.fxml"));

        // 1. Imposta dimensioni: 1000 larghezza, 600 altezza
        Scene scene = new Scene(loader.load(), 1000, 600);

        // 2. Collega il file CSS
        String css = this.getClass().getResource("/com/dipartimento/prova_scan/style.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Gestione Scadenze Prodotti");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}