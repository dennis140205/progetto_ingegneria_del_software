package com.dipartimento.prova_scan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * --- PATTERN SINGLETON ---
 * Questa classe è un Singleton. Garantisce che esista una sola istanza
 * del DatabaseManager per gestire la connessione al database.
 */
public class DatabaseManager {

    // --- Inizio Pattern Singleton ---
    private static DatabaseManager instance;
    private final String url = "jdbc:sqlite:prodotti.db";

    /**
     * Il costruttore è privato per impedire l'istanziazione
     * esterna (es. con 'new DatabaseManager()').
     */
    private DatabaseManager() {
        creaTabellaSeNonEsiste();
    }

    /**
     * Metodo statico per ottenere l'unica istanza della classe.
     * È 'synchronized' per essere sicuro in caso di multi-threading.
     * @return L'unica istanza di DatabaseManager.
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    // --- Fine Pattern Singleton ---


    private void creaTabellaSeNonEsiste() {
        // (Logica invariata...)
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


    public void aggiungiProdotto(Prodotto p) {
        // (Logica invariata...)
        String sql = "INSERT INTO prodotti(nome, marca, categoria, barcode, data_scadenza, quantità) VALUES(?,?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getMarca());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getBarcode());
            ps.setString(5, p.getDataScadenza().toString());
            ps.setInt(6, p.getQuantità());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }


    public void aggiornaProdotto(Prodotto p) {
        // (Logica invariata...)
        String sql = "UPDATE prodotti SET nome = ?, marca = ?, categoria = ?, barcode = ?, data_scadenza = ?, quantità = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getMarca());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getBarcode());
            ps.setString(5, p.getDataScadenza().toString());
            ps.setInt(6, p.getQuantità());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }


    public List<Prodotto> getProdotti() {
        // (Logica invariata...)
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


    public Prodotto cercaPerBarcode(String barcode) {
        // (Logica invariata...)
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