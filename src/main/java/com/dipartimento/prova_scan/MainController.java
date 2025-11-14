package com.dipartimento.prova_scan;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class MainController {

    @FXML private TableView<Prodotto> tabellaProdotti;
    @FXML private TableColumn<Prodotto, String> colNome, colMarca, colCategoria, colBarcode;
    @FXML private TableColumn<Prodotto, LocalDate> colScadenza;

    private DatabaseManager db = new DatabaseManager();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colScadenza.setCellValueFactory(new PropertyValueFactory<>("dataScadenza"));

        aggiornaTabella();
        NotificheManager.controllaScadenze(db.getProdotti());

        tabellaProdotti.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Prodotto p, boolean empty) {
                super.updateItem(p, empty);
                if (p == null || empty) { setStyle(""); return; }
                LocalDate oggi = LocalDate.now();
                if (p.getDataScadenza().isBefore(oggi)) setStyle("-fx-background-color: #ffb3b3;");
                else if (p.getDataScadenza().isBefore(oggi.plusDays(3))) setStyle("-fx-background-color: #fff1b3;");
                else setStyle("");
            }
        });
    }

    private void aggiornaTabella() {
        List<Prodotto> prodotti = db.getProdotti();
        tabellaProdotti.getItems().setAll(prodotti);
    }

    @FXML
    public void scansionaBarcode() {
        BarcodeScanner scanner = new BarcodeScanner();
        Stage stage = (Stage) tabellaProdotti.getScene().getWindow();

        scanner.startScanner(stage, codice -> {
            Prodotto p = db.cercaPerBarcode(codice);
            if (p != null) {
                mostraMessaggio("Prodotto già nel database:\n" + p.getNome());
            } else {
                var info = OpenFootFactsAPI.getProdottoByBarcode(codice);
                Platform.runLater(() -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/aggiungiProdotto.fxml"));
                        Parent root = loader.load();
                        AggiungiProdottoController controller = loader.getController();
                        if (info != null) controller.precompilaCampi(info);
                        Stage finestra = new Stage();
                        finestra.setTitle("Aggiungi prodotto");
                        finestra.setScene(new Scene(root));
                        finestra.showAndWait();
                        aggiornaTabella();
                    } catch (IOException e) { e.printStackTrace(); }
                });
            }
        });
    }

    private void mostraMessaggio(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info prodotto");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

