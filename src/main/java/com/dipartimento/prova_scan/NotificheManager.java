package com.dipartimento.prova_scan;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.TextArea;

public class NotificheManager {

    // Flag per assicurarci di inviare l'email solo una volta per sessione
    private static boolean emailInviataQuestaSessione = false;

    /**
     * Controlla i prodotti all'avvio dell'app.
     * Mostra un Alert e invia un'email se ci sono prodotti scaduti O in scadenza.
     */
    public static void controllaScadenze(List<Prodotto> prodotti) {
        LocalDate oggi = LocalDate.now();

        // --- INIZIO MODIFICA ---

        // Lista 1: Prodotti GIÀ SCADUTI (data prima di oggi)
        List<Prodotto> giaScaduti = prodotti.stream()
                .filter(p -> p.getDataScadenza().isBefore(oggi))
                .collect(Collectors.toList());

        // Lista 2: Prodotti IN SCADENZA (data oggi o nei prossimi 3 giorni)
        List<Prodotto> inScadenza = prodotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi)) // Non scaduti
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3))) // Ma entro 3gg
                .collect(Collectors.toList());

        // Se entrambe le liste sono vuote, non fare nulla
        if (giaScaduti.isEmpty() && inScadenza.isEmpty()) {
            return;
        }

        // Costruiamo il messaggio separando i due gruppi
        StringBuilder msgBuilder = new StringBuilder();

        if (!giaScaduti.isEmpty()) {
            msgBuilder.append("--- PRODOTTI SCADUTI ---\n");
            msgBuilder.append(
                    giaScaduti.stream()
                            .map(NotificheManager::formattaMessaggioProdotto) // Usa l'helper
                            .collect(Collectors.joining("\n"))
            );
            msgBuilder.append("\n\n"); // Aggiungi uno spazio
        }

        if (!inScadenza.isEmpty()) {
            msgBuilder.append("--- IN SCADENZA (Entro 3 giorni) ---\n");
            msgBuilder.append(
                    inScadenza.stream()
                            .map(NotificheManager::formattaMessaggioProdotto) // Usa l'helper
                            .collect(Collectors.joining("\n"))
            );
        }

        String msg = msgBuilder.toString();
        // --- FINE MODIFICA ---


        if (!msg.isEmpty()) {
            // 1. Mostra l'alert visivo (con testo aggiornato)
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Avviso Scadenze");
                alert.setHeaderText("Attenzione! Hai prodotti scaduti e in scadenza!");

                // Impostiamo il contenuto in una TextArea per una migliore leggibilità
                TextArea textArea = new TextArea(msg);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                alert.getDialogPane().setContent(textArea);

                alert.showAndWait();
            });

            // 2. Invia l'email (solo una volta per sessione)
            if (!emailInviataQuestaSessione) {
                System.out.println("Prodotti scaduti o in scadenza rilevati. Tentativo di invio email...");

                new Thread(() -> {
                    EmailManager.inviaEmailScadenza(msg); // Invia il messaggio formattato
                    emailInviataQuestaSessione = true;
                }).start();
            }
        }
    }

    /**
     * --- METODO HELPER NUOVO ---
     * Formatta la stringa per un singolo prodotto, per evitare duplicazione di codice.
     */
    private static String formattaMessaggioProdotto(Prodotto p) {
        return "- " + p.getNome()
                + " (Scade il: " + p.getDataScadenza() + ")"
                + " - Quantità: " + p.getQuantità();
    }
}