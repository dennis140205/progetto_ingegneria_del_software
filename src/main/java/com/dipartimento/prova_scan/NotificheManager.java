package com.dipartimento.prova_scan;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import java.util.List;

/**
 * --- PATTERN STRATEGY (Context) ---
 * Questa classe è configurata con una ConcreteStrategy (Compositor)
 * per eseguire la logica di notifica.
 *
 * Corrisponde al "Context" (o "Composition") del tuo schema.
 */
public class NotificheManager {

    private static boolean emailInviataQuestaSessione = false;

    // Il Context mantiene un riferimento alla Strategy
    private static Compositor strategiaDiNotifica = new SimpleCompositor();

    /**
     * Controlla i prodotti all'avvio dell'app.
     * Delega alla strategia il compito di creare il messaggio.
     */
    public static void controllaScadenze(List<Prodotto> prodotti) {

        // --- INIZIO MODIFICA STRATEGY ---
        // 1. Usa la strategia (impostata di default)
        String msg = strategiaDiNotifica.creaMessaggioNotifica(prodotti);
        // --- FINE MODIFICA STRATEGY ---


        // 2. Il resto della logica (il "come" notificare) rimane invariato.
        if (msg != null && !msg.isEmpty()) {

            // Mostra l'alert visivo
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Avviso Scadenze");
                alert.setHeaderText("Attenzione! Hai prodotti scaduti e in scadenza!");

                TextArea textArea = new TextArea(msg);
                textArea.setEditable(false);
                textArea.setWrapText(true);
                alert.getDialogPane().setContent(textArea);

                alert.showAndWait();
            });

            // Invia l'email
            if (!emailInviataQuestaSessione) {
                System.out.println("Prodotti scaduti o in scadenza rilevati. Tentativo di invio email...");

                new Thread(() -> {
                    EmailManager.inviaEmailScadenza(msg);
                    emailInviataQuestaSessione = true;
                }).start();
            }
        }
    }

    // (Metodo helper rimosso, ora è in SimpleCompositor)
}