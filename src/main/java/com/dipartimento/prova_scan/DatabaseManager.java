package com.dipartimento.prova_scan;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private final String url = "jdbc:sqlite:prodotti.db";

    public DatabaseManager() {
        creaTabellaSeNonEsiste();
    }

    private void creaTabellaSeNonEsiste() {
        String sql = """
            CREATE TABLE IF NOT EXISTS prodotti (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT,
                marca TEXT,
                categoria TEXT,
                barcode TEXT UNIQUE,
                data_scadenza TEXT
            )
        """;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void aggiungiProdotto(Prodotto p) {
        String sql = "INSERT OR REPLACE INTO prodotti(nome, marca, categoria, barcode, data_scadenza) VALUES(?,?,?,?,?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getMarca());
            ps.setString(3, p.getCategoria());
            ps.setString(4, p.getBarcode());
            ps.setString(5, p.getDataScadenza().toString());
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

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
                        LocalDate.parse(rs.getString("data_scadenza"))
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

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
                        LocalDate.parse(rs.getString("data_scadenza"))
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}
