module com.dipartimento.prova_scan {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.dipartimento.prova_scan to javafx.fxml;
    exports com.dipartimento.prova_scan;
}