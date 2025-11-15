package com.dipartimento.prova_scan;

import java.time.LocalDate;

/**
 * --- PATTERN BUILDER (Interfaccia) ---
 * Definisce i passi astratti per costruire un Prodotto.
 * Corrisponde al "Builder" (es. TextConverter) del tuo esempio.
 */
public interface ProdottoBuilder {

    // Metodi per costruire le varie parti del prodotto
    void buildId(int id);
    void buildNome(String nome);
    void buildMarca(String marca);
    void buildCategoria(String categoria);
    void buildBarcode(String barcode);
    void buildDataScadenza(LocalDate dataScadenza);
    void buildQuantità(int quantità);

    /**
     * Metodo per recuperare il prodotto finale.
     * Corrisponde al "GetASCIIText" del tuo esempio.
     * @return Il Prodotto costruito.
     */
    Prodotto getProdotto();
}