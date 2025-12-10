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
    @FXML private Spinner<Integer> campoQuantita;
    @FXML private Button btnScanBarcode;
    @FXML private Button btnSearchBarcode;

    private Prodotto prodottoDaModificare = null;
    private DatabaseManager db = DatabaseManager.getInstance();

    @FXML
    public void initialize() {
        campoQuantita.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));

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
                        // Titolo finestra vs Intestazione interna
                        mostraAlert(Alert.AlertType.WARNING,
                                "Errore di Formato",
                                "Data non riconosciuta",
                                "Impossibile analizzare la data: " + scannedDate + ". Usare GG/MM/AAAA.");
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
            mostraAlert(Alert.AlertType.WARNING,
                    "Campo vuoto",
                    "Barcode mancante",
                    "Inserisci un codice a barre per cercare.");
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
                mostraAlert(Alert.AlertType.INFORMATION,
                        "Risultato Ricerca",
                        "Prodotto non trovato",
                        "Barcode non trovato né localmente né online. Inserisci i dati manualmente.");
            }
        }
    }

    public void initializeWithProduct(Prodotto prodotto) {
        this.prodottoDaModificare = prodotto;
        precompilaCampiEsistenti(prodotto);

        // In modifica blocchiamo il barcode
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
        campoQuantita.getValueFactory().setValue(p.getQuantità());
    }

    @FXML
    private void salvaProdotto() {
        String nome = campoNome.getText();
        String marca = campoMarca.getText();
        String categoria = campoCategoria.getText();
        String barcode = campoBarcode.getText();
        LocalDate scadenza = dataScadenza.getValue();
        int quantitàInserita = campoQuantita.getValue();

        if (nome.isEmpty() || scadenza == null) {
            mostraAlert(Alert.AlertType.WARNING,
                    "Validazione",
                    "Dati obbligatori mancanti",
                    "Inserisci almeno il Nome e la Data di Scadenza.");
            return;
        }

        List<Prodotto> prodottiEsistenti = db.getProdotti();

        if (prodottoDaModificare != null) {
            // --- MODIFICA ---
            Prodotto match = null;
            for (Prodotto p : prodottiEsistenti) {
                if (p.getId() == prodottoDaModificare.getId()) continue;
                boolean barcodeMatch = !barcode.isEmpty() && barcode.equals(p.getBarcode()) && scadenza.equals(p.getDataScadenza());
                boolean nameMatch = !nome.isEmpty() && nome.equals(p.getNome()) && scadenza.equals(p.getDataScadenza());
                if (barcodeMatch || nameMatch) {
                    match = p;
                    break;
                }
            }

            if (match != null) {
                int nuovaQuantitàTotale = match.getQuantità() + quantitàInserita;
                match.setQuantità(nuovaQuantitàTotale);
                db.aggiornaProdotto(match);
                db.eliminaProdotto(prodottoDaModificare.getId());

                mostraAlert(Alert.AlertType.INFORMATION,
                        "Gestione Inventario",
                        "Prodotti Uniti",
                        "Il prodotto è stato unito a uno esistente.\nNuova quantità totale: " + nuovaQuantitàTotale);
            } else {
                Prodotto p = new Prodotto(prodottoDaModificare.getId(), nome, marca, categoria, barcode, scadenza, quantitàInserita);
                db.aggiornaProdotto(p);

                mostraAlert(Alert.AlertType.INFORMATION,
                        "Gestione Inventario",
                        "Modifica Completata",
                        "Il prodotto è stato aggiornato con successo.");
            }

        } else {
            // --- AGGIUNTA ---
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
                int nuovaQuantitàTotale = match.getQuantità() + quantitàInserita;
                match.setQuantità(nuovaQuantitàTotale);
                db.aggiornaProdotto(match);

                mostraAlert(Alert.AlertType.INFORMATION,
                        "Gestione Inventario",
                        "Prodotto Aggregato",
                        "Prodotto già presente. Quantità aggiornata a: " + nuovaQuantitàTotale);
            } else {
                Prodotto p = new Prodotto(0, nome, marca, categoria, barcode, scadenza, quantitàInserita);
                db.aggiungiProdotto(p);

                mostraAlert(Alert.AlertType.INFORMATION,
                        "Gestione Inventario",
                        "Operazione Completata",
                        "Nuovo prodotto inserito correttamente.");
            }
        }

        Stage stage = (Stage) btnSalva.getScene().getWindow();
        stage.close();
    }

    /**
     * Metodo aggiornato per accettare Titolo Finestra e Header Intestazione separati.
     */
    private void mostraAlert(Alert.AlertType type, String titoloFinestra, String headerInterno, String messaggio) {
        Alert alert = new Alert(type);
        alert.setTitle(titoloFinestra); // Titolo sulla barra della finestra
        alert.setHeaderText(headerInterno); // Titolo dentro la finestra (area grigia)
        alert.setContentText(messaggio);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/com/dipartimento/prova_scan/style.css").toExternalForm()
        );

        alert.showAndWait();
    }
}