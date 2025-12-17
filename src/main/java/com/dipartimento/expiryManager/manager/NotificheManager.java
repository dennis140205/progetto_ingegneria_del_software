package com.dipartimento.expiryManager.manager;

import com.dipartimento.expiryManager.services.email.EmailManager;
import com.dipartimento.expiryManager.model.Prodotto;
import com.dipartimento.expiryManager.model.strategy.StrategiaNotifica;
import com.dipartimento.expiryManager.model.strategy.StrategiaNotificaTesto;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// Gestisce la logica di controllo scadenze
public class NotificheManager {

    // Flag thread-safe per garantire che l'email parta una sola volta per sessione.
    private static final AtomicBoolean emailInviata = new AtomicBoolean(false);

    // Definisce l'algoritmo di formattazione del messaggio
    private static final StrategiaNotifica strategiaDiNotifica = new StrategiaNotificaTesto();

    public static void controllaScadenze(List<Prodotto> prodotti) {
        LocalDate oggi = LocalDate.now();

        // Filtra i prodotti già scaduti
        List<Prodotto> scaduti = prodotti.stream()
                .filter(p -> p.getDataScadenza().isBefore(oggi))
                .collect(Collectors.toList());

        // Filtra i prodotti in scadenza nei prossimi 3 giorni
        List<Prodotto> inScadenza = prodotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi))
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3)))
                .collect(Collectors.toList());

        if (scaduti.isEmpty() && inScadenza.isEmpty()) {
            return;
        }

        // Unisce le liste per la notifica email
        List<Prodotto> prodottiDaNotificare = Stream.concat(scaduti.stream(), inScadenza.stream()).toList();

        // Aggiorna la GUI nel thread dedicato di JavaFX
        Platform.runLater(() -> mostraNotifica(scaduti, inScadenza, oggi));

        // Invia l'email in un thread separato (in background) se non è ancora stata inviata
        if (emailInviata.compareAndSet(false, true)) {
            String messaggio = strategiaDiNotifica.creaMessaggio(prodottiDaNotificare);

            if (messaggio != null && !messaggio.isBlank()) {
                new Thread(() -> EmailManager.inviaEmailScadenza(messaggio)).start();
            }
        }
    }

    // Costruisce e mostra il pop-up di avviso
    private static void mostraNotifica(List<Prodotto> scaduti, List<Prodotto> inScadenza, LocalDate oggi) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Notifiche Scadenze");
        alert.setHeaderText("Riepilogo prodotti");
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(550);

        VBox contentBox = new VBox(8);
        contentBox.getStyleClass().add("notifica-box");

        // Sezione scaduti
        if (!scaduti.isEmpty()) {
            Label titolo = new Label("SCADUTI");
            titolo.getStyleClass().add("notifica-titolo-sezione");
            contentBox.getChildren().add(titolo);

            for (Prodotto p : scaduti) {
                Label label = new Label(String.format("%s (%s) [Qtà: %d] - Scaduto il: %s", p.getNome(), p.getMarca(), p.getQuantita(), p.getDataScadenza()));
                label.getStyleClass().add("notifica-item-scaduto");
                label.setMaxWidth(Double.MAX_VALUE);
                contentBox.getChildren().add(label);
            }
        }

        // Sezione in scadenza
        if (!inScadenza.isEmpty()) {
            Label titolo = new Label("IN SCADENZA");
            titolo.getStyleClass().add("notifica-titolo-sezione");
            contentBox.getChildren().add(titolo);

            for (Prodotto p : inScadenza) {
                long giorni = ChronoUnit.DAYS.between(oggi, p.getDataScadenza());

                // Formatta il messaggio in base ai giorni mancanti
                String testo = switch ((int) giorni) {
                    case 0 -> String.format("%s (%s) [Qtà: %d] - Scade oggi!", p.getNome(), p.getMarca(), p.getQuantita());
                    case 1 -> String.format("%s (%s) [Qtà: %d] - Scade domani", p.getNome(), p.getMarca(), p.getQuantita());
                    default -> String.format("%s (%s) [Qtà: %d] - Scade il %s (tra %d giorni)", p.getNome(), p.getMarca(), p.getQuantita(), p.getDataScadenza(), giorni);
                };

                Label label = new Label(testo);
                label.getStyleClass().add("notifica-item-in-scadenza");
                label.setMaxWidth(Double.MAX_VALUE);
                contentBox.getChildren().add(label);
            }
        }

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(Math.min((scaduti.size() + inScadenza.size()) * 45 + 120, 400));

        alert.getDialogPane().setContent(scrollPane);

        // Carica CSS se presente, ignorando errori se manca
        try {
            alert.getDialogPane().getStylesheets().add(NotificheManager.class.getResource("/css/style.css").toExternalForm());
        } catch (Exception ignored) {}

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        alert.showAndWait();
    }
}