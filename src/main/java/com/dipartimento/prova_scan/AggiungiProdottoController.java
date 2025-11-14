package com.dipartimento.prova_scan;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AggiungiProdottoController {

    @FXML private TextField campoNome, campoMarca, campoCategoria, campoBarcode;
    @FXML private DatePicker dataScadenza;
    @FXML private Button btnSalva;
    @FXML private Button btnScanDate;


    private DatabaseManager db = new DatabaseManager();

    @FXML
    public void initialize() {
        btnScanDate.setOnAction(e -> {
            DateScanner ds = new DateScanner();
            ds.start(null, scannedDate -> {
                dataScadenza.setValue(LocalDate.parse(scannedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            });
        });

    }

    public void precompilaCampi(OpenFootFactsAPI.ProdottoInfo info) {
        campoNome.setText(info.nome);
        campoMarca.setText(info.marca);
        campoCategoria.setText(info.categoria);
        campoBarcode.setText(info.barcode);
    }

    private void salvaProdotto() {
        String nome = campoNome.getText();
        String marca = campoMarca.getText();
        String categoria = campoCategoria.getText();
        String barcode = campoBarcode.getText();
        LocalDate scadenza = dataScadenza.getValue();

        if (nome.isEmpty() || scadenza == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Errore");
            alert.setHeaderText("Campi obbligatori mancanti");
            alert.setContentText("Inserisci almeno nome e data di scadenza");
            alert.showAndWait();
            return;
        }

        Prodotto p = new Prodotto(0, nome, marca, categoria, barcode, scadenza);
        db.aggiungiProdotto(p);

        Stage stage = (Stage) btnSalva.getScene().getWindow();
        stage.close();
    }
}

