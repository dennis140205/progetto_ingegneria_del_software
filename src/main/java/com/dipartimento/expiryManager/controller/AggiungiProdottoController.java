package com.dipartimento.expiryManager.controller;


import com.dipartimento.expiryManager.model.Prodotto;
import com.dipartimento.expiryManager.services.scanner.BarcodeScanner;
import com.dipartimento.expiryManager.services.db.DatabaseManager;
import com.dipartimento.expiryManager.services.scanner.DateScanner;
import com.dipartimento.expiryManager.services.api.OpenFootFactsAPI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Gestisce la finestra di dialogo per l'aggiunta o la modifica di un prodotto.
public class AggiungiProdottoController {

    @FXML private TextField campoNome, campoMarca, campoCategoria, campoBarcode;
    @FXML private DatePicker dataScadenza;
    @FXML private Button btnSalva;
    @FXML private Button btnScanDate;
    @FXML private Spinner<Integer> campoQuantita;
    @FXML private Button btnScanBarcode;
    @FXML private Button btnSearchBarcode;

    private Prodotto prodottoDaModificare = null;
    private DatabaseManager db = DatabaseManager.getInstance();

    @FXML
    public void initialize() {
        // Configurazione per la quantità (min 1, max 999)
        campoQuantita.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
    }

    // Avvia il servizio di scansione OCR per rilevare la data di scadenza dalla webcam
    @FXML
    private void scansionaData() {
        Stage currentStage = (Stage) btnScanDate.getScene().getWindow();
        DateScanner dateScanner = new DateScanner();
        dateScanner.start(currentStage, scannedDate -> {
            if (scannedDate != null) {
                try {
                    // Parsing della data rilevata e aggiornamento UI
                    LocalDate parsedDate = LocalDate.parse(scannedDate, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    dataScadenza.setValue(parsedDate);
                } catch (Exception ex) {
                    System.err.println("Data non valida: " + scannedDate);
                    mostraAlert(Alert.AlertType.WARNING, "Errore Data", "Data non riconosciuta.");
                }
            }
        });
    }


    // Avvia la scansione del codice a barre tramite webcam
    @FXML
    private void scansionaEInserisciBarcode() {
        Stage currentStage = (Stage) btnScanBarcode.getScene().getWindow();
        BarcodeScanner scanner = new BarcodeScanner();
        scanner.startScanner(currentStage, codice -> {
            if (codice != null) {
                Platform.runLater(() -> {
                    campoBarcode.setText(codice);
                    // Una volta scansionato, cerca automaticamente i dettagli
                    cercaInfoBarcode();
                });
            }
        });
    }

    // Cerca le informazioni del prodotto: prima nel DB locale, poi tramite API esterna
    @FXML
    private void cercaInfoBarcode() {
        String barcode = campoBarcode.getText();
        if (barcode == null || barcode.isEmpty()) {
            mostraAlert(Alert.AlertType.WARNING, "Attenzione", "Inserisci o scansiona un barcode.");
            return;
        }

        // Ricerca nel DB locale
        Prodotto prodottoLocale = db.cercaPerBarcode(barcode);
        if (prodottoLocale != null) {
            precompilaCampiDaProdottoLocale(prodottoLocale);
        } else {
            // Ricerca su API OpenFoodFacts
            var info = OpenFootFactsAPI.getProdottoByBarcode(barcode);
            if (info != null) {
                precompilaCampiDaApi(info);
            } else {
                mostraAlert(Alert.AlertType.INFORMATION, "Info", "Prodotto non trovato. Inserisci i dati manualmente.");
            }
        }
    }

    // Metodo chiamato dal MainController quando si vuole modificare un prodotto esistente
    public void initializeWithProduct(Prodotto prodotto) {
        this.prodottoDaModificare = prodotto;
        precompilaCampiEsistenti(prodotto);
        // Blocca la modifica del barcode in fase di modifica per evitare incongruenze
        campoBarcode.setEditable(false);
        campoBarcode.setDisable(true);
        btnScanBarcode.setDisable(true);
        btnSearchBarcode.setDisable(true);
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
        campoQuantita.getValueFactory().setValue(p.getQuantita());
    }

    // Logica principale di salvataggio: gestisce inserimento e modifica, inclusa l'aggregazione di prodotti identici
    @FXML
    private void salvaProdotto() {
        String nome = campoNome.getText();
        String marca = campoMarca.getText();
        String categoria = campoCategoria.getText();
        String barcode = campoBarcode.getText();
        LocalDate scadenza = dataScadenza.getValue();
        int quantitaInserita = campoQuantita.getValue();

        if (nome == null || nome.isBlank() || scadenza == null) {
            mostraAlert(Alert.AlertType.WARNING, "Dati Mancanti", "Nome e Data Scadenza sono obbligatori.");
            return;
        }

        List<Prodotto> prodottiEsistenti = db.getProdotti();

        // Modifica di un prodotto esistente
        if (prodottoDaModificare != null) {
            Prodotto match = null;
            // Controlla se le modifiche hanno reso questo prodotto identico a un altro già presente
            for (Prodotto p : prodottiEsistenti) {
                if (p.getId() == prodottoDaModificare.getId()) continue; // Salta se stesso

                // Criterio di uguaglianza: stesso Barcode+Scadenza oppure stesso Nome+Scadenza
                boolean stessoBarcode = !barcode.isEmpty() && barcode.equals(p.getBarcode()) && scadenza.equals(p.getDataScadenza());
                boolean stessoNome = nome.equals(p.getNome()) && scadenza.equals(p.getDataScadenza());

                if (stessoBarcode || stessoNome) {
                    match = p;
                    break;
                }
            }

            if (match != null) {
                // Aggiorna la quantità del prodotto trovato e rimuove quello vecchio (unione)
                match.setQuantita(match.getQuantita() + quantitaInserita);
                db.aggiornaProdotto(match);
                db.eliminaProdotto(prodottoDaModificare.getId());

                mostraAlert(Alert.AlertType.INFORMATION, "Unione Prodotti", "Prodotto unito. Nuova quantità: " + match.getQuantita());
            } else {
                // Aggiornamento standard senza unione
                Prodotto aggiornato = new Prodotto(prodottoDaModificare.getId(), nome, marca, categoria, barcode, scadenza, quantitaInserita);
                db.aggiornaProdotto(aggiornato);
                mostraAlert(Alert.AlertType.INFORMATION, "Modifica Completata", "Il prodotto è stato aggiornato.");
            }

        } else {
            // Inserimento di un nuovo prodotto
            Prodotto match = null;

            // Verifica se il prodotto esiste già per aggregare la quantità
            for (Prodotto p : prodottiEsistenti) {
                boolean stessoBarcode = !barcode.isEmpty() && barcode.equals(p.getBarcode()) && scadenza.equals(p.getDataScadenza());
                boolean stessoNome = nome.equals(p.getNome()) && scadenza.equals(p.getDataScadenza());

                if (stessoBarcode || stessoNome) {
                    match = p;
                    break;
                }
            }

            if (match != null) {
                // Aggiorna quantità esistente
                match.setQuantita(match.getQuantita() + quantitaInserita);
                db.aggiornaProdotto(match);

                mostraAlert(Alert.AlertType.INFORMATION, "Prodotto Aggregato", "Quantità aggiornata a: " + match.getQuantita());
            } else {
                // Crea nuovo record
                Prodotto nuovo = new Prodotto(0, nome, marca, categoria, barcode, scadenza, quantitaInserita);
                db.aggiungiProdotto(nuovo);
                mostraAlert(Alert.AlertType.INFORMATION, "Operazione Completata", "Nuovo prodotto inserito.");
            }
        }
        ((Stage) btnSalva.getScene().getWindow()).close();
    }

    private void mostraAlert(Alert.AlertType type, String titolo, String messaggio) {
        Alert alert = new Alert(type);
        alert.setTitle(titolo);
        alert.setHeaderText(titolo);
        alert.setContentText(messaggio);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );

        alert.showAndWait();
    }
}