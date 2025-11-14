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
            Stage currentStage = (Stage) btnScanDate.getScene().getWindow();

            DateScanner ds = new DateScanner();
            ds.start(currentStage, scannedDate -> {

                // --- INIZIO CORREZIONE ---
                // Controlla se la data è nulla (es. l'utente ha chiuso la finestra)
                if (scannedDate != null) {
                    try {
                        // Imposta la data letta
                        dataScadenza.setValue(LocalDate.parse(scannedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    } catch (Exception ex) {
                        System.err.println("Formato data non valido: " + scannedDate);
                        mostraAlert("Errore Formato", "Impossibile analizzare la data: " + scannedDate + ". Usare GG/MM/AAAA.");
                    }
                }
                // --- FINE CORREZIONE ---
            });
        });
    }

    /**
     * Chiamato da MainController DOPO la chiusura dello scanner barcode.
     * Riceve il barcode (o null) e popola la vista.
     */
    public void initializeWithBarcode(String barcode) {
        if (barcode == null) {
            return;
        }

        Prodotto p = db.cercaPerBarcode(barcode);

        if (p != null) {
            mostraAlert("Prodotto già presente", "Questo prodotto è già nel database:\n" + p.getNome() + "\nPuoi modificare la data di scadenza e salvarlo per aggiornarla.");
            precompilaCampiEsistenti(p);
        } else {
            var info = OpenFootFactsAPI.getProdottoByBarcode(barcode);

            if (info != null) {
                precompilaCampi(info);
            } else {
                campoBarcode.setText(barcode);
                mostraAlert("Info non trovate", "Barcode non trovato su OpenFoodFacts. Inserisci i dati manualmente.");
            }
        }
    }

    /**
     * Precompila i campi usando le info dall'API OpenFoodFacts.
     */
    public void precompilaCampi(OpenFootFactsAPI.ProdottoInfo info) {
        campoNome.setText(info.nome);
        campoMarca.setText(info.marca);
        campoCategoria.setText(info.categoria);
        campoBarcode.setText(info.barcode);
    }

    /**
     * Precompila i campi usando un Prodotto già esistente nel DB.
     */
    private void precompilaCampiEsistenti(Prodotto p) {
        campoNome.setText(p.getNome());
        campoMarca.setText(p.getMarca());
        campoCategoria.setText(p.getCategoria());
        campoBarcode.setText(p.getBarcode());
        dataScadenza.setValue(p.getDataScadenza());
    }

    @FXML
    private void salvaProdotto() {
        String nome = campoNome.getText();
        String marca = campoMarca.getText();
        String categoria = campoCategoria.getText();
        String barcode = campoBarcode.getText();
        LocalDate scadenza = dataScadenza.getValue();

        if (nome.isEmpty() || scadenza == null) {
            mostraAlert("Campi obbligatori mancanti", "Inserisci almeno nome e data di scadenza");
            return;
        }

        Prodotto p = new Prodotto(0, nome, marca, categoria, barcode, scadenza);
        db.aggiungiProdotto(p);

        Stage stage = (Stage) btnSalva.getScene().getWindow();
        stage.close();
    }

    /**
     * Helper per mostrare un Alert.
     */
    private void mostraAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}