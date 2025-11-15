package com.dipartimento.prova_scan;

import java.time.LocalDate;

public class Prodotto {
    private int id;
    private String nome;
    private String marca;
    private String categoria;
    private String barcode;
    private LocalDate dataScadenza;
    private int quantità;

    public Prodotto(int id, String nome, String marca, String categoria, String barcode, LocalDate dataScadenza, int quantità) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
        this.categoria = categoria;
        this.barcode = barcode;
        this.dataScadenza = dataScadenza;
        this.quantità = quantità;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getMarca() { return marca; }
    public String getCategoria() { return categoria; }
    public String getBarcode() { return barcode; }
    public LocalDate getDataScadenza() { return dataScadenza; }
    public int getQuantità() { return quantità; }

    public void setId(int id) { this.id = id; }

    // --- AGGIUNTO ---
    // Necessario per aggiornare la quantità dei prodotti duplicati
    public void setQuantità(int quantità) { this.quantità = quantità; }
}