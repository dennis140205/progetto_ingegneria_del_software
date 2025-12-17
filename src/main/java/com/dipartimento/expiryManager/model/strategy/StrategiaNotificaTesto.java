package com.dipartimento.expiryManager.model.strategy;

import com.dipartimento.expiryManager.model.Prodotto;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

// Implementa la logica per creare un messaggio di testo semplice.
public class StrategiaNotificaTesto implements StrategiaNotifica {

    @Override
    public String creaMessaggio(List<Prodotto> tuttiProdotti) {
        LocalDate oggi = LocalDate.now();

        // Filtra i prodotti già scaduti
        List<Prodotto> scaduti = tuttiProdotti.stream()
                .filter(p -> p.getDataScadenza().isBefore(oggi))
                .collect(Collectors.toList());

        // Filtra i prodotti che scadranno nei prossimi 3 giorni (incluso oggi)
        List<Prodotto> inScadenza = tuttiProdotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi))
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3)))
                .collect(Collectors.toList());

        // Se non ci sono avvisi da dare, ritorna null
        if (scaduti.isEmpty() && inScadenza.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        // Sezione Prodotti scaduti
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

        // Sezione Prodotti in ccadenza a breve (entro 3 giorni)
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