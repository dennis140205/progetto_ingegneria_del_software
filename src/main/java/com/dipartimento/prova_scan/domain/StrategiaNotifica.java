package com.dipartimento.prova_scan.domain;

import java.util.List;

/**
 * --- PATTERN STRATEGY (Interfaccia) ---
 * Definisce l'interfaccia comune per tutte le strategie di
 * creazione del messaggio di notifica.
 */
public interface StrategiaNotifica {

    /**
     * Crea il testo del messaggio da inviare o mostrare.
     * @param tuttiProdotti La lista dei prodotti.
     * @return Il messaggio formattato.
     */
    String creaMessaggio(List<Prodotto> tuttiProdotti);
}