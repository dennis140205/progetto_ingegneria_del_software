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
import java.util.stream.Collectors;

public class NotificheManager {

    private static boolean emailInviataQuestaSessione = false;

    // --- PATTERN STRATEGY: Uso dell'interfaccia rinominata ---
    private static StrategiaNotifica strategiaDiNotifica = new StrategiaNotificaTesto();

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

        // GUI: Mostra l'alert a schermo (Layout a blocchi colorati)
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Notifiche Scadenze");
            alert.setHeaderText("Riepilogo prodotti");
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(500);

            VBox contentBox = new VBox(8);
            contentBox.getStyleClass().add("notifica-box");

            if (!scaduti.isEmpty()) {
                Label titolo = new Label("SCADUTI");
                titolo.getStyleClass().add("notifica-titolo-sezione");
                contentBox.getChildren().add(titolo);

                for (Prodotto p : scaduti) {
                    String testo = String.format("%s (%s) - Scaduto il: %s",
                            p.getNome(), p.getMarca(), p.getDataScadenza());
                    Label labelProd = new Label(testo);
                    labelProd.setMaxWidth(Double.MAX_VALUE);
                    labelProd.getStyleClass().add("notifica-item-scaduto");
                    contentBox.getChildren().add(labelProd);
                }
            }

            if (!inScadenza.isEmpty()) {
                Label titolo = new Label("IN SCADENZA");
                titolo.getStyleClass().add("notifica-titolo-sezione");
                contentBox.getChildren().add(titolo);

                for (Prodotto p : inScadenza) {
                    long giorni = ChronoUnit.DAYS.between(oggi, p.getDataScadenza());
                    String testo = String.format("%s (%s) - Scade il %s (tra %d giorni)",
                            p.getNome(), p.getMarca(), p.getDataScadenza(), giorni);
                    if (giorni == 0) testo = p.getNome() + " ("+ p.getMarca() +") - Scade oggi!";
                    if (giorni == 1) testo = p.getNome() + " ("+ p.getMarca() +") - Scade domani";

                    Label labelProd = new Label(testo);
                    labelProd.setMaxWidth(Double.MAX_VALUE);
                    labelProd.getStyleClass().add("notifica-item-in-scadenza");
                    contentBox.getChildren().add(labelProd);
                }
            }

            ScrollPane scrollPane = new ScrollPane(contentBox);
            scrollPane.setFitToWidth(true);

            int numeroElementi = scaduti.size() + inScadenza.size();
            double altezzaNecessaria = (numeroElementi * 45) + 120;
            double altezzaFinale = Math.min(altezzaNecessaria, 400);

            scrollPane.setPrefHeight(altezzaFinale);
            scrollPane.setStyle("-fx-background-color:transparent; -fx-background-insets: 0;");

            alert.getDialogPane().setContent(scrollPane);

            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    NotificheManager.class.getResource("/com/dipartimento/prova_scan/style.css").toExternalForm()
            );

            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.setAlwaysOnTop(true);
            alert.showAndWait();
        });

        // Invio Email: Usa il Pattern Strategy per generare il testo
        if (!emailInviataQuestaSessione) {
            String messaggioEmail = strategiaDiNotifica.creaMessaggio(prodotti);
            if (messaggioEmail != null && !messaggioEmail.isEmpty()) {
                new Thread(() -> {
                    EmailManager.inviaEmailScadenza(messaggioEmail);
                    emailInviataQuestaSessione = true;
                }).start();
            }
        }
    }
}