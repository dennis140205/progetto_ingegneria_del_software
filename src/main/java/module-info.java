module com.dipartimento.prova_scan {
    // --- JavaFX ---
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;   // serve per SwingFXUtils
    requires java.sql;

    // --- Librerie esterne ---
    requires webcam.capture;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.json;
    requires tess4j;

    // --- Pacchetto principale ---
    opens com.dipartimento.prova_scan to javafx.fxml;
    exports com.dipartimento.prova_scan;
}
