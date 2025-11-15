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

    private DatabaseManager db = new DatabaseManager();

    @FXML
    public void initialize() {
        campoQuantità.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

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
                    cercaInfoBarcode(); // Cerca automaticamente dopo lo scan
                });
            }
        });
    }

    /**
     * --- METODO MODIFICATO ---
     * Cerca il barcode prima nel DB locale e poi, se non trovato, online.
     */
    @FXML
    private void cercaInfoBarcode() {
        String barcode = campoBarcode.getText();
        if (barcode == null || barcode.isEmpty()) {
            mostraAlert("Barcode mancante", "Inserisci un codice a barre per cercare.");
            return;
        }

        // 1. Cerca nel database locale
        Prodotto prodottoLocale = db.cercaPerBarcode(barcode); //

        if (prodottoLocale != null) {
            // 2. Trovato nel DB: precompila i campi da lì
            precompilaCampiDaProdottoLocale(prodottoLocale);

        } else {
            // 3. Non trovato nel DB: cerca online
            var info = OpenFootFactsAPI.getProdottoByBarcode(barcode); //

            if (info != null) {
                // 4. Trovato online: precompila da API
                precompilaCampiDaApi(info);
            } else {
                // 5. Non trovato da nessuna parte
                mostraAlert("Info non trovate", "Barcode non trovato né nel DB locale né online. Puoi inserire i dati manualmente.");
            }
        }
    }


    /**
     * Chiamato da MainController quando si clicca "Modifica".
     */
    public void initializeWithProduct(Prodotto prodotto) {
        this.prodottoDaModificare = prodotto;

        precompilaCampiEsistenti(prodotto);

        campoBarcode.setEditable(false);
        btnScanBarcode.setDisable(true);
        btnSearchBarcode.setDisable(true);
    }

    /**
     * Precompila i campi usando le info dall'API (Rinominato da precompilaCampi).
     */
    private void precompilaCampiDaApi(OpenFootFactsAPI.ProdottoInfo info) {
        campoNome.setText(info.nome);
        campoMarca.setText(info.marca);
        campoCategoria.setText(info.categoria);
        campoBarcode.setText(info.barcode);
    }

    /**
     * --- METODO NUOVO ---
     * Precompila i campi usando i dati di un prodotto già nel DB locale.
     * Non imposta la data di scadenza o la quantità.
     */
    private void precompilaCampiDaProdottoLocale(Prodotto p) {
        campoNome.setText(p.getNome());
        campoMarca.setText(p.getMarca());
        campoCategoria.setText(p.getCategoria());
        // Non impostiamo data di scadenza o quantità
    }

    /**
     * Precompila TUTTI i campi usando un Prodotto esistente (per la modalità Modifica).
     */
    private void precompilaCampiEsistenti(Prodotto p) {
        campoNome.setText(p.getNome());
        campoMarca.setText(p.getMarca());
        campoCategoria.setText(p.getCategoria());
        campoBarcode.setText(p.getBarcode());
        dataScadenza.setValue(p.getDataScadenza());
        campoQuantità.getValueFactory().setValue(p.getQuantità());
    }

    /**
     * Salva il prodotto (logica di aggiunta/modifica/duplicati)
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
            Prodotto p = new Prodotto(
                    prodottoDaModificare.getId(),
                    nome, marca, categoria, barcode, scadenza, quantitàAggiunta
            );
            db.aggiornaProdotto(p);
        } else {
            // --- Siamo in MODALITÀ AGGIUNGI ---
            List<Prodotto> prodottiEsistenti = db.getProdotti();
            Prodotto match = null;

            for (Prodotto p : prodottiEsistenti) {
                // Controlla se il barcode (se esiste) E la data corrispondono
                boolean barcodeMatch = !barcode.isEmpty() && barcode.equals(p.getBarcode()) && scadenza.equals(p.getDataScadenza());

                // Controlla se il nome E la data corrispondono
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
                // Non trovato. Aggiungi come prodotto completamente nuovo.
                Prodotto p = new Prodotto(0, nome, marca, categoria, barcode, scadenza, quantitàAggiunta);
                db.aggiungiProdotto(p);
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
}