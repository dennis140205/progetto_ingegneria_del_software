package com.dipartimento.prova_scan;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class NotificheManager {

    // Flag per assicurarci di inviare l'email solo una volta per sessione
    private static boolean emailInviataQuestaSessione = false;

    /**
     * Controlla i prodotti all'avvio dell'app.
     * Mostra un Alert e invia un'email se ci sono prodotti in scadenza.
     */
    public static void controllaScadenze(List<Prodotto> prodotti) {
        LocalDate oggi = LocalDate.now();
        List<Prodotto> inScadenza = prodotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi)) // Non ancora scaduti
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3))) // Ma in scadenza entro 3 giorni
                .collect(Collectors.toList());

        if (!inScadenza.isEmpty()) {
            // Costruisce il messaggio
            String msg = inScadenza.stream()
                    .map(p -> "- " + p.getNome() + " (Scade il: " + p.getDataScadenza() + ")")
                    .collect(Collectors.joining("\n"));

            // 1. Mostra l'alert visivo (sempre)
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Prodotti in scadenza");
                alert.setHeaderText("Attenzione! Hai prodotti in scadenza!");
                alert.setContentText(msg);
                alert.showAndWait();
            });

            // 2. Invia l'email (solo una volta per sessione)
            if (!emailInviataQuestaSessione) {
                System.out.println("Prodotti in scadenza rilevati. Tentativo di invio email...");

                // Avvia l'invio in un thread separato per non bloccare la UI
                new Thread(() -> {
                    EmailManager.inviaEmailScadenza(msg);
                    emailInviataQuestaSessione = true; // Imposta il flag
                }).start();
            }
        }
    }
}