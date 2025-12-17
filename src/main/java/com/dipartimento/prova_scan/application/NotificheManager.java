package com.dipartimento.prova_scan.application;

import com.dipartimento.prova_scan.services.EmailManager;
import com.dipartimento.prova_scan.domain.Prodotto;
import com.dipartimento.prova_scan.domain.StrategiaNotifica;
import com.dipartimento.prova_scan.domain.StrategiaNotificaTesto;
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

public class NotificheManager {

    // Thread-safe: email inviata una sola volta per sessione
    private static final AtomicBoolean emailInviata = new AtomicBoolean(false);

    // Pattern Strategy
    private static final StrategiaNotifica strategiaDiNotifica = new StrategiaNotificaTesto();

    public static void controllaScadenze(List<Prodotto> prodotti) {
        LocalDate oggi = LocalDate.now();

        List<Prodotto> scaduti = prodotti.stream()
                .filter(p -> p.getDataScadenza().isBefore(oggi))
                .collect(Collectors.toList());

        List<Prodotto> inScadenza = prodotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi))
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3)))
                .collect(Collectors.toList());

        if (scaduti.isEmpty() && inScadenza.isEmpty()) {
            return;
        }

        // Lista dei prodotti da notificare
        List<Prodotto> prodottiDaNotificare = Stream.concat(scaduti.stream(), inScadenza.stream()).toList();

        // --- GUI ---
        Platform.runLater(() -> mostraNotifica(scaduti, inScadenza, oggi));

        // --- EMAIL (una sola volta) ---
        if (emailInviata.compareAndSet(false, true)) {
            String messaggio = strategiaDiNotifica.creaMessaggio(prodottiDaNotificare);

            if (messaggio != null && !messaggio.isBlank()) {
                new Thread(() -> EmailManager.inviaEmailScadenza(messaggio)).start();
            }
        }
    }

    // --- METODO GUI SEPARATO (più pulito) ---
    private static void mostraNotifica(List<Prodotto> scaduti, List<Prodotto> inScadenza, LocalDate oggi) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Notifiche Scadenze");
        alert.setHeaderText("Riepilogo prodotti");
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(550);

        VBox contentBox = new VBox(8);
        contentBox.getStyleClass().add("notifica-box");

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

        if (!inScadenza.isEmpty()) {
            Label titolo = new Label("IN SCADENZA");
            titolo.getStyleClass().add("notifica-titolo-sezione");
            contentBox.getChildren().add(titolo);

            for (Prodotto p : inScadenza) {
                long giorni = ChronoUnit.DAYS.between(oggi, p.getDataScadenza());

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

        try {
            alert.getDialogPane().getStylesheets().add(NotificheManager.class.getResource("/com/dipartimento/prova_scan/style.css").toExternalForm());
        } catch (Exception ignored) {}

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        alert.showAndWait();
    }
}