package com.dipartimento.prova_scan;

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
    private static Compositor strategiaDiNotifica = new SimpleCompositor();

    public static void controllaScadenze(List<Prodotto> prodotti) {
        LocalDate oggi = LocalDate.now();

        // 1. Filtri
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

        // 2. Costruzione Interfaccia
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Avviso Scadenze");
            alert.setHeaderText("Riepilogo prodotti");
            alert.setResizable(true);

            // --- MODIFICA LARGHEZZA ---
            // Impostiamo una larghezza fissa più ampia (es. 500px)
            alert.getDialogPane().setPrefWidth(500);
            // --------------------------

            VBox contentBox = new VBox(8);
            contentBox.getStyleClass().add("notifica-box");

            // Sezione SCADUTI
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

            // Sezione IN SCADENZA
            if (!inScadenza.isEmpty()) {
                Label titolo = new Label("IN SCADENZA");
                titolo.getStyleClass().add("notifica-titolo-sezione");
                contentBox.getChildren().add(titolo);

                for (Prodotto p : inScadenza) {
                    long giorni = ChronoUnit.DAYS.between(oggi, p.getDataScadenza());
                    String testo = String.format("%s (%s) - Scade il %s (tra %d giorni)",
                            p.getNome(), p.getMarca(), p.getDataScadenza(), giorni);

                    if (giorni == 0) testo = p.getNome()+" (" + p.getMarca()+ ") - Scade oggi!";
                    if (giorni == 1) testo = p.getNome() +" (" + p.getMarca()+ ") - Scade domani!";

                    Label labelProd = new Label(testo);
                    labelProd.setMaxWidth(Double.MAX_VALUE);
                    labelProd.getStyleClass().add("notifica-item-in-scadenza");
                    contentBox.getChildren().add(labelProd);
                }
            }

            // --- LOGICA ALTEZZA (Mantenuta dalla modifica precedente) ---
            ScrollPane scrollPane = new ScrollPane(contentBox);
            scrollPane.setFitToWidth(true);

            int numeroElementi = scaduti.size() + inScadenza.size();
            double altezzaNecessaria = (numeroElementi * 45) + 100;
            double altezzaFinale = Math.min(altezzaNecessaria, 400); // Max 400px altezza

            scrollPane.setPrefHeight(altezzaFinale);
            // -----------------------------------------------------------

            scrollPane.setStyle("-fx-background-color:transparent; -fx-background-insets: 0; -fx-padding: 0;");

            alert.getDialogPane().setContent(scrollPane);

            // Carica CSS
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    NotificheManager.class.getResource("/com/dipartimento/prova_scan/style.css").toExternalForm()
            );

            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.setAlwaysOnTop(true);
            alert.showAndWait();
        });

        // Email
        if (!emailInviataQuestaSessione) {
            String messaggioEmail = strategiaDiNotifica.creaMessaggioNotifica(prodotti);
            if (messaggioEmail != null && !messaggioEmail.isEmpty()) {
                new Thread(() -> {
                    EmailManager.inviaEmailScadenza(messaggioEmail);
                    emailInviataQuestaSessione = true;
                }).start();
            }
        }
    }
}