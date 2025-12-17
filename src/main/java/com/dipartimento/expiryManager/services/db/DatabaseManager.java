package com.dipartimento.expiryManager.services.db;

import com.dipartimento.expiryManager.model.Prodotto;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//Garantisce che esista una sola istanza del DatabaseManager per gestire la connessione
public class DatabaseManager {

    private static DatabaseManager instance;
    private final String url = "jdbc:sqlite:prodotti.db";

    private DatabaseManager() {
        creaTabella();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }


    // Inizializza lo schema del database creando la tabella se non esiste
    private void creaTabella() {
        String sql = """
            CREATE TABLE IF NOT EXISTS prodotti (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT,
                marca TEXT,
                categoria TEXT,
                barcode TEXT, 
                data_scadenza TEXT,
                quantità INTEGER
            )
        """;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) { e.printStackTrace(); }
    }


    // Inserisce un nuovo record nel database
    public void aggiungiProdotto(Prodotto p) {
        String sql = "INSERT INTO prodotti(nome, marca, categoria, barcode, data_scadenza, quantità) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getMarca());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getBarcode());
            ps.setString(5, p.getDataScadenza().toString());
            ps.setInt(6, p.getQuantita());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }


    // Aggiorna i dati di un prodotto esistente
    public void aggiornaProdotto(Prodotto p) {
        String sql = "UPDATE prodotti SET nome = ?, marca = ?, categoria = ?, barcode = ?, data_scadenza = ?, quantità = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getMarca());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getBarcode());
            ps.setString(5, p.getDataScadenza().toString());
            ps.setInt(6, p.getQuantita());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }


    // Recupera tutti i prodotti e li converte in oggetti Java
    public List<Prodotto> getProdotti() {
        List<Prodotto> lista = new ArrayList<>();
        String sql = "SELECT * FROM prodotti";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Prodotto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("marca"),
                        rs.getString("categoria"),
                        rs.getString("barcode"),
                        LocalDate.parse(rs.getString("data_scadenza")),
                        rs.getInt("quantità")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }


    // Ricerca per codice a barre
    public Prodotto cercaPerBarcode(String barcode) {
        String sql = "SELECT * FROM prodotti WHERE barcode = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Prodotto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("marca"),
                        rs.getString("categoria"),
                        rs.getString("barcode"),
                        LocalDate.parse(rs.getString("data_scadenza")),
                        rs.getInt("quantità")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // Rimuove un prodotto dal DB tramite id
    public void eliminaProdotto(int idProdotto) {
        // (Logica invariata...)
        String sql = "DELETE FROM prodotti WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idProdotto);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
}