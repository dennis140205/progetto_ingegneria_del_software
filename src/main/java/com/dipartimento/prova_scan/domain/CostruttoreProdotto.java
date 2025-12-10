package com.dipartimento.prova_scan.domain;

import java.time.LocalDate;

/**
 * --- PATTERN BUILDER (Interfaccia) ---
 * Definisce i passi per costruire un oggetto Prodotto.
 */
public interface CostruttoreProdotto {
    void impostaId(int id);
    void impostaNome(String nome);
    void impostaMarca(String marca);
    void impostaCategoria(String categoria);
    void impostaBarcode(String barcode);
    void impostaScadenza(LocalDate data);
    void impostaQuantita(int quantita);

    /**
     * Restituisce il prodotto costruito.
     */
    Prodotto costruisci();
}