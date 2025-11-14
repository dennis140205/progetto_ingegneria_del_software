package com.dipartimento.prova_scan;

// (Tutti gli import rimangono invariati)
import javafx.application.Platform; // Questo import non è più strettamente necessario qui, ma non dà fastidio
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
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
        // (Il tuo codice initialize rimane invariato)
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
        Stage mainStage = (Stage) tabellaProdotti.getScene().getWindow();
        BarcodeScanner scanner = new BarcodeScanner();

        // Avvia lo scanner (che blocca 'mainStage')
        scanner.startScanner(mainStage, codice -> {

            // --- INIZIO CORREZIONE ---
            // Rimosso Platform.runLater(). Il codice viene eseguito immediatamente
            // dato che 'scanner.startScanner' ci chiama già sul thread JavaFX.

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/aggiungiProdotto.fxml"));
                Parent root = loader.load();
                AggiungiProdottoController controller = loader.getController();

                Stage finestraAggiungi = new Stage();
                finestraAggiungi.setTitle("Aggiungi prodotto");
                finestraAggiungi.setScene(new Scene(root));

                finestraAggiungi.initOwner(mainStage);
                finestraAggiungi.initModality(Modality.APPLICATION_MODAL);

                controller.initializeWithBarcode(codice);

                finestraAggiungi.showAndWait();

                // DOPO che la finestra è stata chiusa, aggiorna la tabella
                aggiornaTabella();

            } catch (IOException e) {
                e.printStackTrace();
                mostraMessaggio("Errore nell'apertura della finestra Aggiungi");
            }
            // --- FINE CORREZIONE ---
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