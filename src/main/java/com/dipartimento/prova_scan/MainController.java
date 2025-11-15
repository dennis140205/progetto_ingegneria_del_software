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

    @FXML private TextField campoEmailMittente;
    @FXML private PasswordField campoPasswordApp;
    @FXML private TextField campoEmailNotifiche;

    // --- MODIFICA SINGLETON ---
    // Ottiene l'unica istanza del DatabaseManager invece di crearne una nuova.
    private DatabaseManager db = DatabaseManager.getInstance();
    // --- FINE MODIFICA ---

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colBarcode.setCellValueFactory(new PropertyValueFactory<>("barcode"));
        colScadenza.setCellValueFactory(new PropertyValueFactory<>("dataScadenza"));
        colQuantità.setCellValueFactory(new PropertyValueFactory<>("quantità"));

        aggiornaTabella();
        NotificheManager.controllaScadenze(db.getProdotti());

        // Carica tutte le impostazioni salvate all'avvio
        Properties userProps = ConfigManager.getUserProperties();
        campoEmailMittente.setText(userProps.getProperty("mail.username", ""));
        campoPasswordApp.setText(userProps.getProperty("mail.password", ""));
        campoEmailNotifiche.setText(userProps.getProperty("mail.to", ""));

        // (Logica RowFactory invariata...)
        tabellaProdotti.setRowFactory(tv -> {
            TableRow<Prodotto> row = new TableRow<>() {
                @Override
                protected void updateItem(Prodotto p, boolean empty) {
                    super.updateItem(p, empty);
                    if (p == null || empty) {
                        setStyle("");
                    } else {
                        LocalDate oggi = LocalDate.now();
                        if (p.getDataScadenza().isBefore(oggi)) setStyle("-fx-background-color: #ffb3b3;");
                        else if (p.getDataScadenza().isBefore(oggi.plusDays(3))) setStyle("-fx-background-color: #fff1b3;");
                        else setStyle("");
                    }
                }
            };

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
        // (Logica invariata...)
        String mittente = campoEmailMittente.getText();
        String password = campoPasswordApp.getText();
        String destinatario = campoEmailNotifiche.getText();

        if (mittente.isEmpty() || !mittente.contains("@") ||
                password.isEmpty() ||
                destinatario.isEmpty() || !destinatario.contains("@")) {

            mostraMessaggio("Campi incompleti", "Inserisci email mittente, password app e email destinatario validi.");
            return;
        }

        ConfigManager.saveUserSettings(mittente, password, destinatario);
        mostraMessaggio("Impostazioni Salvare", "Le tue credenziali email sono state salvate in modo sicuro sul tuo computer.");
    }

    private void aggiornaTabella() {
        // (Logica invariata...)
        List<Prodotto> prodotti = db.getProdotti();
        tabellaProdotti.getItems().setAll(prodotti);
    }

    private void apriFinestraModifica(Prodotto prodotto) {
        // (Logica invariata...)
        try {
            Stage mainStage = (Stage) tabellaProdotti.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/aggiungiProdotto.fxml"));
            Parent root = loader.load();

            AggiungiProdottoController controller = loader.getController();

            Stage finestraAggiungi = new Stage();
            finestraAggiungi.setTitle("Modifica prodotto");
            finestraAggiungi.setScene(new Scene(root));
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
        // (Logica invariata...)
        try {
            Stage mainStage = (Stage) tabellaProdotti.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/dipartimento/prova_scan/aggiungiProdotto.fxml"));
            Parent root = loader.load();
            AggiungiProdottoController controller = loader.getController();

            Stage finestraAggiungi = new Stage();
            finestraAggiungi.setTitle("Aggiungi prodotto");
            finestraAggiungi.setScene(new Scene(root));

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
        // (Logica invariata...)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}