package com.dipartimento.prova_scan;

import java.time.LocalDate;

public class Prodotto {
    private int id;
    private String nome;
    private String marca;
    private String categoria;
    private String barcode;
    private LocalDate dataScadenza;

    public Prodotto(int id, String nome, String marca, String categoria, String barcode, LocalDate dataScadenza) {
        this.id = id;
        this.nome = nome;
        this.marca = marca;
        this.categoria = categoria;
        this.barcode = barcode;
        this.dataScadenza = dataScadenza;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getMarca() { return marca; }
    public String getCategoria() { return categoria; }
    public String getBarcode() { return barcode; }
    public LocalDate getDataScadenza() { return dataScadenza; }

    public void setNome(String nome) { this.nome = nome; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    public void setDataScadenza(LocalDate dataScadenza) { this.dataScadenza = dataScadenza; }
}
