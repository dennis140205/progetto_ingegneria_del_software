package com.dipartimento.prova_scan;

import java.time.LocalDate;

/**
 * --- PATTERN BUILDER (Implementazione) ---
 * Costruisce l'oggetto Prodotto passo dopo passo.
 */
public class CostruttoreProdottoStandard implements CostruttoreProdotto {

    private int id = 0;
    private String nome;
    private String marca = "";
    private String categoria = "";
    private String barcode = "";
    private LocalDate scadenza;
    private int quantita = 0;

    @Override
    public void impostaId(int id) {
        this.id = id;
    }

    @Override
    public void impostaNome(String nome) {
        this.nome = nome;
    }

    @Override
    public void impostaMarca(String marca) {
        if (marca != null) this.marca = marca;
    }

    @Override
    public void impostaCategoria(String categoria) {
        if (categoria != null) this.categoria = categoria;
    }

    @Override
    public void impostaBarcode(String barcode) {
        if (barcode != null) this.barcode = barcode;
    }

    @Override
    public void impostaScadenza(LocalDate data) {
        this.scadenza = data;
    }

    @Override
    public void impostaQuantita(int quantita) {
        this.quantita = quantita;
    }

    @Override
    public Prodotto costruisci() {
        return new Prodotto(id, nome, marca, categoria, barcode, scadenza, quantita);
    }
}