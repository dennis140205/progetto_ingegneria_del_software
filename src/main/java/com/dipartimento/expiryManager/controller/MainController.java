package com.dipartimento.expiryManager.controller;

import com.dipartimento.expiryManager.util.ConfigManager;
import com.dipartimento.expiryManager.services.db.DatabaseManager;
import com.dipartimento.expiryManager.manager.NotificheManager;
import com.dipartimento.expiryManager.model.Prodotto;
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
import java.util.Properties;

// Gestisce la schermata principale, la visualizzazione della tabella prodotti e le impostazioni utente.
public class MainController {

    @FXML private TableView<Prodotto> tabellaProdotti;
    @FXML private TableColumn<Prodotto, String> colNome, colMarca, colCategoria, colBarcode;
    @FXML private TableColumn<Prodotto, LocalDate> colScadenza;
    @FXML private TableColumn<Prodotto, Integer> colQuantita;
    @FXML private TextField campoEmailNotifiche;

    private DatabaseManager db = DatabaseManager.getInstance();

    @FXML
    public void initialize() {
        // Mapping colonne tabella --> attributi oggetto Prodotto
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colScadenza.setCellValueFactory(new PropertyValueFactory<>("dataScadenza"));
        colQuantita.setCellValueFactory(new PropertyValueFactory<>("quantita"));

        // Caricamento dati e avvio controllo scadenze
        aggiornaTabella();
        NotificheManager.controllaScadenze(db.getProdotti());

        // Caricamento configurazione email utente
        Properties userProps = ConfigManager.getUserProperties();
        campoEmailNotifiche.setText(userProps.getProperty("mail.to", ""));

        // Configurazione righe tabella per evidenziare prodotti scaduti/in scadenza
        tabellaProdotti.setRowFactory(tv -> {
            TableRow<Prodotto> row = new TableRow<>() {
                @Override
                protected void updateItem(Prodotto p, boolean empty) {
                    super.updateItem(p, empty);
                    // Reset stile
                    getStyleClass().removeAll("scaduto", "in-scadenza");
                    if (p == null || empty) return;

                    // Applicazione classi CSS
                    LocalDate oggi = LocalDate.now();
                    if (p.getDataScadenza().isBefore(oggi)) {
                        getStyleClass().add("scaduto");
                    } else if (p.getDataScadenza().isBefore(oggi.plusDays(3))) {
                        getStyleClass().add("in-scadenza");
                    }
                }
            };

            // Creazione menù
            final MenuItem modificaMenuItem = new MenuItem("Modifica");
            modificaMenuItem.setOnAction(event -> {
                Prodotto prodottoDaModificare = row.getItem();
                apriFinestraModifica(prodottoDaModificare);
            });

            final MenuItem eliminaMenuItem = new MenuItem("Elimina");
            eliminaMenuItem.setOnAction(event -> {
                Prodotto prodotto = row.getItem();

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Gestione Inventario");      // Titolo finestra (
                alert.setHeaderText("Conferma Eliminazione"); // Intestazione interna
                alert.setContentText("Vuoi davvero eliminare \"" + prodotto.getNome() + "\"?\nQuesta operazione è irreversibile.");
                applicaStile(alert);

                alert.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        db.eliminaProdotto(prodotto.getId());
                        aggiornaTabella();
                    }
                });
            });

            final ContextMenu contextMenu = new ContextMenu();
            contextMenu.getItems().addAll(modificaMenuItem, eliminaMenuItem);

            // Associa il menu solo se la riga non è vuota
            row.contextMenuProperty().bind(row.emptyProperty().map(empty -> empty ? null : contextMenu));

            // Gestione doppio click per aprire menu contestuale (alternativa al tasto destro)
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });

            return row;
        });
    }

    // Salva l'email per le notifiche nel file di configurazione utente
    @FXML
    private void salvaImpostazioniEmail() {
        String destinatario = campoEmailNotifiche.getText();
        // Validazione formato email semplice
        if (!destinatario.matches(".+@.+\\..+"))
        {
            mostraMessaggio(Alert.AlertType.WARNING,
                    "Errore Validazione",
                    "Email non valida",
                    "Inserisci un'email destinatario valida.");
            return;
        }
        ConfigManager.saveUserEmail(destinatario);
        mostraMessaggio(Alert.AlertType.INFORMATION,
                "Configurazione",
                "Impostazioni Salvate",
                "Riceverai le notifiche a: " + destinatario);
    }

    private void aggiornaTabella() {
        tabellaProdotti.getItems().setAll(db.getProdotti());
    }

    // Apre la finestra per modificare un prodotto esistente
    private void apriFinestraModifica(Prodotto prodotto) {
        try {
            Stage mainStage = (Stage) tabellaProdotti.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/aggiungiProdotto.fxml"));
            Parent root = loader.load();
            AggiungiProdottoController controller = loader.getController();

            Stage finestraAggiungi = new Stage();
            finestraAggiungi.setTitle("Modifica prodotto");
            Scene scene = new Scene(root, 500, 650);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            finestraAggiungi.setScene(scene);
            finestraAggiungi.initOwner(mainStage);
            finestraAggiungi.initModality(Modality.APPLICATION_MODAL); // Blocca la finestra principale

            // Inizializza il controller con i dati del prodotto selezionato
            controller.initializeWithProduct(prodotto);

            finestraAggiungi.showAndWait(); // Attende la chiusura
            aggiornaTabella(); // Ricarica i dati
        } catch (IOException e) {
            e.printStackTrace();
            mostraMessaggio(Alert.AlertType.ERROR, "Errore Applicazione", "Errore Critico", "Impossibile aprire la finestra di modifica.");
        }
    }

    // Apre la finestra per aggiungere un nuovo prodotto
    @FXML
    public void apriFinestraAggiungi() {
        try {
            Stage mainStage = (Stage) tabellaProdotti.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/aggiungiProdotto.fxml"));
            Parent root = loader.load();
            Stage finestraAggiungi = new Stage();
            finestraAggiungi.setTitle("Aggiungi prodotto");
            Scene scene = new Scene(root, 500, 650);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            finestraAggiungi.setScene(scene);
            finestraAggiungi.initOwner(mainStage);
            finestraAggiungi.initModality(Modality.APPLICATION_MODAL);
            finestraAggiungi.showAndWait();
            aggiornaTabella();
        } catch (IOException e) {
            e.printStackTrace();
            mostraMessaggio(Alert.AlertType.ERROR, "Errore Applicazione", "Errore Critico", "Impossibile aprire la finestra di aggiunta.");
        }
    }

    private void mostraMessaggio(Alert.AlertType type, String titoloFinestra, String headerInterno, String messaggio) {
        Alert alert = new Alert(type);
        alert.setTitle(titoloFinestra);
        alert.setHeaderText(headerInterno);
        alert.setContentText(messaggio);
        applicaStile(alert);
        alert.showAndWait();
    }

    private void applicaStile(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/style.css").toExternalForm()
        );
    }
}