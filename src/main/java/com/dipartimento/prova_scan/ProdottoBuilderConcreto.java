package com.dipartimento.prova_scan;

import java.time.LocalDate;

/**
 * --- PATTERN BUILDER (Implementazione Concreta) ---
 * Implementa l'interfaccia Builder per costruire un Prodotto.
 * Mantiene lo stato dell'oggetto in costruzione.
 * Corrisponde al "ConcreteBuilder" (es. ASCIIConverter) del tuo esempio.
 */
public class ProdottoBuilderConcreto implements ProdottoBuilder {

    // Campi per memorizzare lo stato in costruzione
    private int id = 0; // Default
    private String nome;
    private String marca = "";
    private String categoria = "";
    private String barcode = "";
    private LocalDate dataScadenza;
    private int quantità;

    @Override
    public void buildId(int id) {
        this.id = id;
    }

    @Override
    public void buildNome(String nome) {
        this.nome = nome;
    }

    @Override
    public void buildMarca(String marca) {
        this.marca = (marca != null) ? marca : "";
    }

    @Override
    public void buildCategoria(String categoria) {
        this.categoria = (categoria != null) ? categoria : "";
    }

    @Override
    public void buildBarcode(String barcode) {
        this.barcode = (barcode != null) ? barcode : "";
    }

    @Override
    public void buildDataScadenza(LocalDate dataScadenza) {
        this.dataScadenza = dataScadenza;
    }

    @Override
    public void buildQuantità(int quantità) {
        this.quantità = quantità;
    }

    /**
     * Restituisce il "Product" finale.
     * Qui viene chiamato il costruttore della classe Prodotto
     * con tutti i campi assemblati.
     */
    @Override
    public Prodotto getProdotto() {
        // Valori obbligatori (nome, scadenza)
        // La validazione è già gestita dal Director (AggiungiProdottoController)
        return new Prodotto(id, nome, marca, categoria, barcode, dataScadenza, quantità);
    }
}