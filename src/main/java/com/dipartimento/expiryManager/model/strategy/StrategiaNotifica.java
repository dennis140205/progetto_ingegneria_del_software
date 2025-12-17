package com.dipartimento.expiryManager.model.strategy;

import com.dipartimento.expiryManager.model.Prodotto;

import java.util.List;

// Definisce il contratto comune per tutti gli algoritmi di notifica.
// Permette di cambiare il formato del messaggio senza modificare il codice che lo usa.
public interface StrategiaNotifica {

    // Genera il testo del messaggio per una lista di prodotti
    String creaMessaggio(List<Prodotto> prodotti);
}