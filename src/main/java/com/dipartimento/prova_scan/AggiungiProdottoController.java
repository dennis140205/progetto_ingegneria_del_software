package com.dipartimento.prova_scan;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * --- PATTERN BUILDER (Director) ---
 * Questa classe (in particolare il metodo 'salvaProdotto')
 * agisce come "Director". Orchestra la costruzione di un Prodotto
 * utilizzando un'istanza di ProdottoBuilder.
 */
public class AggiungiProdottoController {

    @FXML private TextField campoNome, campoMarca, campoCategoria, campoBarcode;
    @FXML private DatePicker dataScadenza;
    @FXML private Button btnSalva;
    @FXML private Button btnScanDate;
    @FXML private Spinner<Integer> campoQuantità;

    @FXML private Button btnScanBarcode;
    @FXML private Button btnSearchBarcode;

    private Prodotto prodottoDaModificare = null;

    // Usa il Singleton
    private DatabaseManager db = DatabaseManager.getInstance();

    @FXML
    public void initialize() {
        // (Logica invariata...)
        campoQuantità.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        btnScanDate.setOnAction(e -> {
            Stage currentStage = (Stage) btnScanDate.getScene().getWindow();
            DateScanner ds = new DateScanner();
            ds.start(currentStage, scannedDate -> {

                if (scannedDate != null) {
                    try {
                        LocalDate parsedDate;

                        if (scannedDate.length() == 10) {
                            parsedDate = LocalDate.parse(scannedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } else if (scannedDate.length() == 8) {
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
        // (Logica invariata...)
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
        // (Logica invariata...)
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
                mostraAlert("Info non trovate", "Barcode non trovato né nel DB locale né online. Puoi inserire i dati manualmente.");
            }
        }
    }


    public void initializeWithProduct(Prodotto prodotto) {
        // (Logica invariata...)
        this.prodottoDaModificare = prodotto;
        precompilaCampiEsistenti(prodotto);

        campoBarcode.setEditable(false);
        btnScanBarcode.setDisable(true);
        btnSearchBarcode.setDisable(true);
    }


    private void precompilaCampiDaApi(OpenFootFactsAPI.ProdottoInfo info) {
        // (Logica invariata...)
        campoNome.setText(info.nome);
        campoMarca.setText(info.marca);
        campoCategoria.setText(info.categoria);
        campoBarcode.setText(info.barcode);
    }


    private void precompilaCampiDaProdottoLocale(Prodotto p) {
        // (Logica invariata...)
        campoNome.setText(p.getNome());
        campoMarca.setText(p.getMarca());
        campoCategoria.setText(p.getCategoria());
    }


    private void precompilaCampiEsistenti(Prodotto p) {
        // (Logica invariata...)
        campoNome.setText(p.getNome());
        campoMarca.setText(p.getMarca());
        campoCategoria.setText(p.getCategoria());
        campoBarcode.setText(p.getBarcode());
        dataScadenza.setValue(p.getDataScadenza());
        campoQuantità.getValueFactory().setValue(p.getQuantità());
    }

    /**
     * Salva il prodotto (logica di aggiunta/modifica/duplicati)
     * Questo metodo agisce da DIRECTOR.
     */
    @FXML
    private void salvaProdotto() {
        String nome = campoNome.getText();
        String marca = campoMarca.getText();
        String categoria = campoCategoria.getText();
        String barcode = campoBarcode.getText();
        LocalDate scadenza = dataScadenza.getValue();
        int quantitàAggiunta = campoQuantità.getValue();

        if (nome.isEmpty() || scadenza == null) {
            mostraAlert("Campi obbligatori mancanti", "Inserisci almeno nome e data di scadenza");
            return;
        }


        if (prodottoDaModificare != null) {
            // --- Siamo in MODALITÀ MODIFICA ---

            // --- INIZIO PATTERN BUILDER (Director) ---
            // 1. Crea il builder
            ProdottoBuilder builder = new ProdottoBuilderConcreto();

            // 2. Orchestra la costruzione (passi)
            builder.buildId(prodottoDaModificare.getId()); // ID esistente
            builder.buildNome(nome);
            builder.buildMarca(marca);
            builder.buildCategoria(categoria);
            builder.buildBarcode(barcode);
            builder.buildDataScadenza(scadenza);
            builder.buildQuantità(quantitàAggiunta);

            // 3. Ottieni il prodotto finito
            Prodotto p = builder.getProdotto();
            // --- FINE PATTERN BUILDER ---

            db.aggiornaProdotto(p);

        } else {
            // --- Siamo in MODALITÀ AGGIUNGI ---
            List<Prodotto> prodottiEsistenti = db.getProdotti();
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
                // Trovato! Aggiorna la quantità di quello esistente.
                int nuovaQuantità = match.getQuantità() + quantitàAggiunta;
                match.setQuantità(nuovaQuantità);
                db.aggiornaProdotto(match);
            } else {
                // Non trovato. Aggiungi come nuovo.

                // --- INIZIO PATTERN BUILDER (Director) ---
                ProdottoBuilder builder = new ProdottoBuilderConcreto();

                // Orchestra la costruzione (l'ID è 0 di default)
                builder.buildNome(nome);
                builder.buildDataScadenza(scadenza);
                builder.buildQuantità(quantitàAggiunta);
                builder.buildMarca(marca);
                builder.buildCategoria(categoria);
                builder.buildBarcode(barcode);

                Prodotto p = builder.getProdotto();
                // --- FINE PATTERN BUILDER ---

                db.aggiungiProdotto(p);
            }
        }

        Stage stage = (Stage) btnSalva.getScene().getWindow();
        stage.close();
    }

    private void mostraAlert(String titolo, String messaggio) {
        // (Logica invariata...)
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}