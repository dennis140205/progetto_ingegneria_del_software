package com.dipartimento.prova_scan.domain;

import java.time.LocalDate;

public class Prodotto {
    private int id;
    private String nome;
    private String marca;
    private String categoria;
    private String barcode;
    private LocalDate dataScadenza;
    private int quantita;

    public Prodotto(int id, String nome, String marca, String categoria, String barcode, LocalDate dataScadenza, int quantita) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
        this.categoria = categoria;
        this.barcode = barcode;
        this.dataScadenza = dataScadenza;
        this.quantita = quantita;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public LocalDate getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(LocalDate dataScadenza) { this.dataScadenza = dataScadenza; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    @Override
    public String toString() {
        return nome + " (" + marca + ")";
    }
}