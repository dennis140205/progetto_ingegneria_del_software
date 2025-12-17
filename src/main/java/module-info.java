module com.dipartimento.prova_scan {
    // JavaFX
    requires javafx.controls; // Controlli UI standard
    requires javafx.fxml;     // Gestione file FXML
    requires javafx.swing; // Per SwingFXUtils (usato negli scanner per convertire immagini)
    requires java.sql;     // Per DatabaseManager (JDBC/SQLite)

    // Librerie Esterne
    requires webcam.capture;      // Driver per acquisizione video
    requires com.google.zxing;    // Core libreria Barcode
    requires com.google.zxing.javase; // Estensioni JavaSE per Barcode
    requires org.json;            // Parsing JSON (API OpenFoodFacts)
    requires tess4j;              // Motore OCR Tesseract (Lettura Date)
    requires jakarta.mail;        // Gestione invio Email (SMTP)
    requires java.desktop;        // Classi AWT/Image necessarie per Webcam e OCR

    // Configurazione package

    exports com.dipartimento.expiryManager.controller;
    opens com.dipartimento.expiryManager.controller to javafx.fxml;
    exports com.dipartimento.expiryManager.model;
    opens com.dipartimento.expiryManager.model to javafx.base;
    exports com.dipartimento.expiryManager;
    opens com.dipartimento.expiryManager to javafx.fxml;
    exports com.dipartimento.expiryManager.model.strategy;
    opens com.dipartimento.expiryManager.model.strategy to javafx.base;
    exports com.dipartimento.expiryManager.services.db;
    exports com.dipartimento.expiryManager.services.email;
    exports com.dipartimento.expiryManager.services.scanner;
    exports com.dipartimento.expiryManager.services.api;
    exports com.dipartimento.expiryManager.manager;
    exports com.dipartimento.expiryManager.util;
}