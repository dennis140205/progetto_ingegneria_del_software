package com.dipartimento.prova_scan;

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
import java.util.Properties;

public class MainController {

    @FXML private TableView<Prodotto> tabellaProdotti;
    @FXML private TableColumn<Prodotto, String> colNome, colMarca, colCategoria, colBarcode;
    @FXML private TableColumn<Prodotto, LocalDate> colScadenza;
    @FXML private TableColumn<Prodotto, Integer> colQuantità;

    // Campo unico per l'email del destinatario (le altre sono fisse/hardcoded)
    @FXML private TextField campoEmailNotifiche;

    private DatabaseManager db = DatabaseManager.getInstance();

    @FXML
    public void initialize() {
        // Setup colonne
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colScadenza.setCellValueFactory(new PropertyValueFactory<>("dataScadenza"));
        colQuantità.setCellValueFactory(new PropertyValueFactory<>("quantità"));

        aggiornaTabella();
        NotificheManager.controllaScadenze(db.getProdotti());

        // Carica solo la mail destinatario salvata
        Properties userProps = ConfigManager.getUserProperties();
        campoEmailNotifiche.setText(userProps.getProperty("mail.to", ""));

        // RowFactory per Colori e Menu Contestuale
        tabellaProdotti.setRowFactory(tv -> {
            TableRow<Prodotto> row = new TableRow<>() {
                @Override
                protected void updateItem(Prodotto p, boolean empty) {
                    super.updateItem(p, empty);
                    if (p == null || empty) {
                        setStyle("");
                    } else {
                        LocalDate oggi = LocalDate.now();

                        // --- COLORI MODERNI (PASTELLO) ---
                        if (p.getDataScadenza().isBefore(oggi)) {
                            // Rosso pastello (Scaduto)
                            setStyle("-fx-background-color: #ffcccc; -fx-border-color: #e74c3c; -fx-border-width: 0 0 0 5;");
                        }
                        else if (p.getDataScadenza().isBefore(oggi.plusDays(3))) {
                            // Giallo pastello (In scadenza)
                            setStyle("-fx-background-color: #fff5cc; -fx-border-color: #f1c40f; -fx-border-width: 0 0 0 5;");
                        }
                        else {
                            // Normale (stile del CSS)
                            setStyle("");
                        }
                    }
                }
            };

            // Menu Contestuale (Tasto Destro)
            final MenuItem modificaMenuItem = new MenuItem("Modifica");
            modificaMenuItem.setOnAction(event -> {
                Prodotto prodottoDaModificare = row.getItem();
                apriFinestraModifica(prodottoDaModificare);
            });

            final MenuItem eliminaMenuItem = new MenuItem("Elimina");
            eliminaMenuItem.setOnAction(event -> {
                Prodotto prodottoDaEliminare = row.getItem();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Conferma Eliminazione");
                alert.setHeaderText("Sei sicuro di voler eliminare il prodotto?");
                alert.setContentText(prodottoDaEliminare.getNome() + " (Quantità: " + prodottoDaEliminare.getQuantità() + ")");

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        db.eliminaProdotto(prodottoDaEliminare.getId());
                        aggiornaTabella();
                    }
                });
            });

            final ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(modificaMenuItem, eliminaMenuItem);

            row.contextMenuProperty().bind(
                    row.emptyProperty().map(empty -> empty ? null : contextMenu)
            );
            return row;
        });
    }


    @FXML
    private void salvaImpostazioniEmail() {
        String destinatario = campoEmailNotifiche.getText();

        if (destinatario.isEmpty() || !destinatario.contains("@")) {
            mostraMessaggio("Email non valida", "Inserisci un'email destinatario valida.");
            return;
        }

        // Salva solo il destinatario nel file utente
        ConfigManager.saveUserEmail(destinatario);

        mostraMessaggio("Email Salvata", "Riceverai le notifiche a: " + destinatario);
    }

    private void aggiornaTabella() {
        List<Prodotto> prodotti = db.getProdotti();
        tabellaProdotti.getItems().setAll(prodotti);
    }

    private void apriFinestraModifica(Prodotto prodotto) {
        try {
            Stage mainStage = (Stage) tabellaProdotti.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/aggiungiProdotto.fxml"));
            Parent root = loader.load();
            AggiungiProdottoController controller = loader.getController();

            Stage finestraAggiungi = new Stage();
            finestraAggiungi.setTitle("Modifica prodotto");

            // --- STILE & DIMENSIONI ---
            Scene scene = new Scene(root, 500, 650);
            scene.getStylesheets().add(getClass().getResource("/com/dipartimento/prova_scan/style.css").toExternalForm());
            finestraAggiungi.setScene(scene);
            // --------------------------

            finestraAggiungi.initOwner(mainStage);
            finestraAggiungi.initModality(Modality.APPLICATION_MODAL);

            controller.initializeWithProduct(prodotto);

            finestraAggiungi.showAndWait();
            aggiornaTabella();

        } catch (IOException e) {
            e.printStackTrace();
            mostraMessaggio("Errore", "Errore nell'apertura della finestra di modifica");
        }
    }

    @FXML
    public void apriFinestraAggiungi() {
        try {
            Stage mainStage = (Stage) tabellaProdotti.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/aggiungiProdotto.fxml"));
            Parent root = loader.load();

            Stage finestraAggiungi = new Stage();
            finestraAggiungi.setTitle("Aggiungi prodotto");

            // --- STILE & DIMENSIONI ---
            Scene scene = new Scene(root, 500, 650);
            scene.getStylesheets().add(getClass().getResource("/com/dipartimento/prova_scan/style.css").toExternalForm());
            finestraAggiungi.setScene(scene);
            // --------------------------

            finestraAggiungi.initOwner(mainStage);
            finestraAggiungi.initModality(Modality.APPLICATION_MODAL);

            finestraAggiungi.showAndWait();
            aggiornaTabella();

        } catch (IOException e) {
            e.printStackTrace();
            mostraMessaggio("Errore", "Errore nell'apertura della finestra Aggiungi");
        }
    }

    private void mostraMessaggio(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}