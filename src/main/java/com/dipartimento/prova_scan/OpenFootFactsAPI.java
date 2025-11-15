package com.dipartimento.prova_scan;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OpenFootFactsAPI {

    public static ProdottoInfo getProdottoByBarcode(String barcode) {
        try {
            String urlStr = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "JavaFXApp-BarcodeScanner/1.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) content.append(line);
            in.close();
            conn.disconnect();

            JSONObject root = new JSONObject(content.toString());
            if (root.getInt("status") == 1) {
                JSONObject prod = root.getJSONObject("product");
                ProdottoInfo info = new ProdottoInfo();
                info.nome = prod.optString("product_name", "Sconosciuto");
                info.marca = prod.optString("brands", "");

                // --- INIZIO MODIFICA CATEGORIA ---
                // Leggi il campo "categories" (più pulito) invece di "categories_tags"
                String categorieRaw = prod.optString("categories", "");

                if (categorieRaw != null && !categorieRaw.isEmpty()) {
                    // Il campo è una stringa tipo: "Bevande, Acque, Acque minerali"
                    String[] listaCategorie = categorieRaw.split(",");

                    // Prendi l'ultima categoria della lista (la più specifica)
                    String categoriaPulita = listaCategorie[listaCategorie.length - 1];

                    // Pulisci da eventuali spazi bianchi
                    info.categoria = categoriaPulita.trim();
                } else {
                    // Fallback se il campo "categories" è vuoto
                    info.categoria = "";
                }
                // --- FINE MODIFICA CATEGORIA ---

                info.immagine = prod.optString("image_url", "");
                info.barcode = barcode;
                return info;
            } else {
                return null;
            }
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public static class ProdottoInfo {
        public String nome;
        public String marca;
        public String categoria;
        public String immagine;
        public String barcode;
    }
}