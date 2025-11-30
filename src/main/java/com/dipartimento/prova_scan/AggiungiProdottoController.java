package com.dipartimento.prova_scan;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AggiungiProdottoController {

    @FXML private TextField campoNome, campoMarca, campoCategoria, campoBarcode;
    @FXML private DatePicker dataScadenza;
    @FXML private Button btnSalva;
    @FXML private Button btnScanDate;
    @FXML private Spinner<Integer> campoQuantità;

    @FXML private Button btnScanBarcode;
    @FXML private Button btnSearchBarcode;

    private Prodotto prodottoDaModificare = null;

    private DatabaseManager db = DatabaseManager.getInstance();

    @FXML
    public void initialize() {
        campoQuantità.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

        btnScanDate.setOnAction(e -> {
            Stage currentStage = (Stage) btnScanDate.getScene().getWindow();
            DateScanner ds = new DateScanner();
            ds.start(currentStage, scannedDate -> {

                if (scannedDate != null) {
                    try {
                        LocalDate parsedDate;

                        if (scannedDate.length() == 10) { // Formato GG/MM/AAAA
                            parsedDate = LocalDate.parse(scannedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

                        } else if (scannedDate.length() == 8) { // Formato GG/MM/AA
                            parsedDate = LocalDate.parse(scannedDate, DateTimeFormatter.ofPattern("dd/MM/yy"));

                        } else {
                            throw new java.time.format.DateTimeParseException("Formato non riconosciuto", scannedDate, 0);
                        }

                        dataScadenza.setValue(parsedDate);

                    } catch (Exception ex) {
                        System.err.println("Formato data non valido: " + scannedDate);
                        mostraAlert("Errore Formato", "Impossibile analizzare la data: " + scannedDate + ". Usare GG/MM/AAAA o GG/MM/AA.");
                    }
                }
            });
        });
    }

    @FXML
    private void scansionaEInserisciBarcode() {
        Stage currentStage = (Stage) btnScanBarcode.getScene().getWindow();
        BarcodeScanner scanner = new BarcodeScanner();

        scanner.startScanner(currentStage, codice -> {
            if (codice != null) {
                Platform.runLater(() -> {
                    campoBarcode.setText(codice);
                    cercaInfoBarcode();
                });
            }
        });
    }

    @FXML
    private void cercaInfoBarcode() {
        String barcode = campoBarcode.getText();
        if (barcode == null || barcode.isEmpty()) {
            mostraAlert("Barcode mancante", "Inserisci un codice a barre per cercare.");
            return;
        }

        Prodotto prodottoLocale = db.cercaPerBarcode(barcode);

        if (prodottoLocale != null) {
            precompilaCampiDaProdottoLocale(prodottoLocale);
        } else {
            var info = OpenFootFactsAPI.getProdottoByBarcode(barcode);
            if (info != null) {
                precompilaCampiDaApi(info);
            } else {
                mostraAlert("Info non trovate", "Barcode non trovato né localmente né online. Puoi inserire i dati manualmente.");
            }
        }
    }


    public void initializeWithProduct(Prodotto prodotto) {
        this.prodottoDaModificare = prodotto;
        precompilaCampiEsistenti(prodotto);

        campoBarcode.setEditable(true);
        btnScanBarcode.setDisable(false);
        btnSearchBarcode.setDisable(false);
    }

    private void precompilaCampiDaApi(OpenFootFactsAPI.ProdottoInfo info) {
        campoNome.setText(info.nome);
        campoMarca.setText(info.marca);
        campoCategoria.setText(info.categoria);
        campoBarcode.setText(info.barcode);
    }

    private void precompilaCampiDaProdottoLocale(Prodotto p) {
        campoNome.setText(p.getNome());
        campoMarca.setText(p.getMarca());
        campoCategoria.setText(p.getCategoria());
    }

    private void precompilaCampiEsistenti(Prodotto p) {
        campoNome.setText(p.getNome());
        campoMarca.setText(p.getMarca());
        campoCategoria.setText(p.getCategoria());
        campoBarcode.setText(p.getBarcode());
        dataScadenza.setValue(p.getDataScadenza());
        campoQuantità.getValueFactory().setValue(p.getQuantità());
    }

    @FXML
    private void salvaProdotto() {
        String nome = campoNome.getText();
        String marca = campoMarca.getText();
        String categoria = campoCategoria.getText();
        String barcode = campoBarcode.getText();
        LocalDate scadenza = dataScadenza.getValue();
        int quantitàInserita = campoQuantità.getValue();

        if (nome.isEmpty() || scadenza == null) {
            mostraAlert("Campi obbligatori mancanti", "Inserisci almeno nome e data di scadenza");
            return;
        }

        List<Prodotto> prodottiEsistenti = db.getProdotti();

        if (prodottoDaModificare != null) {
            // --- Siamo in MODALITÀ MODIFICA ---
            Prodotto match = null;

            for (Prodotto p : prodottiEsistenti) {
                if (p.getId() == prodottoDaModificare.getId()) continue; // Salta se stesso

                boolean barcodeMatch = !barcode.isEmpty() && barcode.equals(p.getBarcode()) && scadenza.equals(p.getDataScadenza());
                boolean nameMatch = !nome.isEmpty() && nome.equals(p.getNome()) && scadenza.equals(p.getDataScadenza());

                if (barcodeMatch || nameMatch) {
                    match = p;
                    break;
                }
            }

            if (match != null) {
                // Caso: Modifica che porta a un duplicato -> Unisci ed elimina vecchio
                int nuovaQuantitàTotale = match.getQuantità() + quantitàInserita;
                match.setQuantità(nuovaQuantitàTotale);

                db.aggiornaProdotto(match);
                db.eliminaProdotto(prodottoDaModificare.getId());

                mostraInfo("Prodotti Uniti", "Il prodotto modificato è stato unito a uno già esistente con la stessa scadenza.\nNuova quantità totale: " + nuovaQuantitàTotale);
            } else {
                // Caso: Modifica normale (NESSUN duplicato)
                Prodotto p = new Prodotto(
                        prodottoDaModificare.getId(),
                        nome, marca, categoria, barcode, scadenza, quantitàInserita
                );
                db.aggiornaProdotto(p);

                // --- MODIFICA QUI ---
                mostraInfo("Modifica Completata", "Il prodotto è stato aggiornato con successo.");
            }

        } else {
            // --- Siamo in MODALITÀ AGGIUNGI ---
            Prodotto match = null;

            for (Prodotto p : prodottiEsistenti) {
                boolean barcodeMatch = !barcode.isEmpty() && barcode.equals(p.getBarcode()) && scadenza.equals(p.getDataScadenza());
                boolean nameMatch = !nome.isEmpty() && nome.equals(p.getNome()) && scadenza.equals(p.getDataScadenza());

                if (barcodeMatch || nameMatch) {
                    match = p;
                    break;
                }
            }

            if (match != null) {
                // Caso: Aggiunta di un duplicato -> Unisci
                int nuovaQuantitàTotale = match.getQuantità() + quantitàInserita;
                match.setQuantità(nuovaQuantitàTotale);
                db.aggiornaProdotto(match);

                mostraInfo("Prodotto aggregato", "Questo prodotto esisteva già in inventario.\nLa quantità è stata aggiornata.\nNuova quantità totale: " + nuovaQuantitàTotale);

            } else {
                // Caso: Nuovo prodotto (NESSUN duplicato)
                Prodotto p = new Prodotto(0, nome, marca, categoria, barcode, scadenza, quantitàInserita);
                db.aggiungiProdotto(p);

                // --- MODIFICA QUI ---
                mostraInfo("Aggiunta Completata", "Il nuovo prodotto è stato inserito correttamente.");
            }
        }

        Stage stage = (Stage) btnSalva.getScene().getWindow();
        stage.close();
    }

    private void mostraAlert(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }

    private void mostraInfo(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}