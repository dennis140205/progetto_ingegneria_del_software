module com.dipartimento.prova_scan {
    // --- JavaFX ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing; // Per SwingFXUtils (usato negli scanner)
    requires java.sql;     // Per DatabaseManager

    // --- Librerie Esterne ---
    requires webcam.capture;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.json;
    requires tess4j;
    requires jakarta.mail;
    // Se usi la libreria java.desktop (awt) implicitamente per le immagini
    requires java.desktop;

    // --- CONFIGURAZIONE PACKAGE ---

    // 1. UI: Contiene Main e Controller.
    //    - 'exports' serve a javafx.graphics per lanciare Main.
    //    - 'opens' serve a javafx.fxml per iniettare i campi @FXML nei controller.
    exports com.dipartimento.prova_scan.ui;
    opens com.dipartimento.prova_scan.ui to javafx.fxml;

    // 2. DOMAIN: Contiene Prodotto.
    //    - 'opens' verso javafx.base è FONDAMENTALE per la TableView (PropertyValueFactory).
    //    - 'exports' rende le entità visibili.
    exports com.dipartimento.prova_scan.domain;
    opens com.dipartimento.prova_scan.domain to javafx.base;

    // 3. APPLICATION: Contiene NotificheManager.
    exports com.dipartimento.prova_scan.application;

    // 4. SERVICES: Contiene Database, Scanner, ecc.
    exports com.dipartimento.prova_scan.services;
}