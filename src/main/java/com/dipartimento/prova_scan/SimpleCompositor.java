package com.dipartimento.prova_scan;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * --- PATTERN STRATEGY (ConcreteStrategy) ---
 * Implementa l'algoritmo di notifica predefinito.
 *
 * Questa strategia notifica:
 * 1. Prodotti GIÀ SCADUTI.
 * 2. Prodotti IN SCADENZA (entro 3 giorni).
 *
 * Corrisponde al "SimpleCompositor" del tuo schema.
 */
public class SimpleCompositor implements Compositor {

    @Override
    public String creaMessaggioNotifica(List<Prodotto> tuttiProdotti) {
        LocalDate oggi = LocalDate.now();

        // Lista 1: Prodotti GIÀ SCADUTI (data prima di oggi)
        List<Prodotto> giaScaduti = tuttiProdotti.stream()
                .filter(p -> p.getDataScadenza().isBefore(oggi))
                .collect(Collectors.toList());

        // Lista 2: Prodotti IN SCADENZA (data oggi o nei prossimi 3 giorni)
        List<Prodotto> inScadenza = tuttiProdotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi)) // Non scaduti
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3))) // Ma entro 3gg
                .collect(Collectors.toList());

        // Se entrambe le liste sono vuote, non c'è nulla da notificare
        if (giaScaduti.isEmpty() && inScadenza.isEmpty()) {
            return null; // Ritorna null
        }

        // Costruiamo il messaggio separando i due gruppi
        StringBuilder msgBuilder = new StringBuilder();

        if (!giaScaduti.isEmpty()) {
            msgBuilder.append("--- PRODOTTI SCADUTI ---\n");
            msgBuilder.append(
                    giaScaduti.stream()
                            .map(this::formattaMessaggioProdotto) // Usa l'helper
                            .collect(Collectors.joining("\n"))
            );
            msgBuilder.append("\n\n"); // Aggiungi uno spazio
        }

        if (!inScadenza.isEmpty()) {
            msgBuilder.append("--- IN SCADENZA (Entro 3 giorni) ---\n");
            msgBuilder.append(
                    inScadenza.stream()
                            .map(this::formattaMessaggioProdotto) // Usa l'helper
                            .collect(Collectors.joining("\n"))
            );
        }

        return msgBuilder.toString();
    }

    /**
     * Formatta la stringa per un singolo prodotto.
     */
    private String formattaMessaggioProdotto(Prodotto p) {
        return "- " + p.getNome()
                + " (Scade il: " + p.getDataScadenza() + ")"
                + " - Quantità: " + p.getQuantità();
    }
}