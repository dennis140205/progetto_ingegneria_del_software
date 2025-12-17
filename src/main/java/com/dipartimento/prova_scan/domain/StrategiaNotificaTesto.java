package com.dipartimento.prova_scan.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * --- PATTERN STRATEGY (Strategia Concreta) ---
 * Implementa la logica per creare un messaggio di testo semplice.
 */
public class StrategiaNotificaTesto implements StrategiaNotifica {

    @Override
    public String creaMessaggio(List<Prodotto> tuttiProdotti) {
        LocalDate oggi = LocalDate.now();

        List<Prodotto> scaduti = tuttiProdotti.stream()
                .filter(p -> p.getDataScadenza().isBefore(oggi))
                .collect(Collectors.toList());

        List<Prodotto> inScadenza = tuttiProdotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi))
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3)))
                .collect(Collectors.toList());

        if (scaduti.isEmpty() && inScadenza.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        if (!scaduti.isEmpty()) {
            sb.append("SCADUTI:\n");
            for (Prodotto p : scaduti) {
                sb.append("- ")
                        .append(p.getNome())
                        .append(" (").append(p.getMarca())
                        .append(") | Qtà: ").append(p.getQuantita())
                        .append(" | Scadenza: ").append(p.getDataScadenza())
                        .append("\n");
            }
            sb.append("\n");
        }

        if (!inScadenza.isEmpty()) {
            sb.append("IN SCADENZA (Entro 3 gg):\n");
            for (Prodotto p : inScadenza) {
                sb.append("- ")
                        .append(p.getNome())
                        .append(" (").append(p.getMarca())
                        .append(") | Qtà: ").append(p.getQuantita())
                        .append(" | Scadenza: ").append(p.getDataScadenza())
                        .append("\n");
            }
        }

        return sb.toString();
    }
}