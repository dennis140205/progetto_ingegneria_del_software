package com.dipartimento.prova_scan;

import java.util.List;

/**
 * --- PATTERN STRATEGY (Strategy) ---
 * Dichiara l'interfaccia comune per tutti gli algoritmi
 * (strategie) di composizione dei messaggi di notifica.
 *
 * Corrisponde al "Compositor" del tuo schema.
 */
public interface Compositor {

    /**
     * Analizza la lista completa dei prodotti e crea un messaggio
     * di notifica formattato.
     *
     * @param tuttiProdotti La lista di tutti i prodotti nel database.
     * @return Una stringa formattata con l'avviso, o null se non c'è nulla da notificare.
     */
    String creaMessaggioNotifica(List<Prodotto> tuttiProdotti);
}