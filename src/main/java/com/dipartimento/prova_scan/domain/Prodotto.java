package com.dipartimento.prova_scan.domain;

import java.time.LocalDate;

/**
 * --- PATTERN BUILDER (Product) ---
 * Rappresenta l'oggetto complesso che stiamo costruendo.
 * È un "POJO" (Plain Old Java Object) con costruttori e getter/setter.
 * Corrisponde al "Product" (es. ASCIIText) del tuo esempio.
 */
public class Prodotto {
    private int id;
    private String nome;
    private String marca;
    private String categoria;
    private String barcode;
    private LocalDate dataScadenza;
    private int quantita;

    /**
     * Costruttore Completo.
     * Usato dal DatabaseManager per caricare oggetti dal DB
     * e dal ProdottoBuilderConcreto per costruire l'oggetto finale.
     */
    public Prodotto(int id, String nome, String marca, String categoria, String barcode, LocalDate dataScadenza, int quantita) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
        this.categoria = categoria;
        this.barcode = barcode;
        this.dataScadenza = dataScadenza;
        this.quantita = quantita;
    }

    // --- Getters (invariati) ---
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getMarca() { return marca; }
    public String getCategoria() { return categoria; }
    public String getBarcode() { return barcode; }
    public LocalDate getDataScadenza() { return dataScadenza; }
    public int getQuantita() { return quantita; }

    // --- Setters (usati per la modifica) ---
    public void setId(int id) { this.id = id; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    // --- Metodi Fluent (RIMOSSI) ---
    // public Prodotto setMarca(String marca) { ... }
    // public Prodotto setCategoria(String categoria) { ... }
    // public Prodotto setBarcode(String barcode) { ... }
}