package com.dipartimento.expiryManager.services.api;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// Interfaccia con l'API pubblica di OpenFoodFacts per recuperare dettagli prodotto tramite barcode.
public class OpenFootFactsAPI {

    public static ProdottoInfo getProdottoByBarcode(String barcode) {
        try {
            // Costruzione URL endpoint
            String urlStr = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            URL url = new URL(urlStr);

            // Configurazione connessione HTTP GET
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // User-Agent necessario per non essere bloccati dall'API
            conn.setRequestProperty("User-Agent", "JavaFXApp-BarcodeScanner/1.0");

            // Lettura della risposta JSON dal server
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) content.append(line);
            in.close();
            conn.disconnect();

            // Parsing del JSON ricevuto
            JSONObject root = new JSONObject(content.toString());

            // Check status: 1 --> prodotto trovato
            if (root.getInt("status") == 1) {
                JSONObject prod = root.getJSONObject("product");
                ProdottoInfo info = new ProdottoInfo();
                info.nome = prod.optString("product_name", "Sconosciuto");
                info.marca = prod.optString("brands", "");

                // Recupero stringa categorie es. cibi, condimenti, salse ecc.
                String categorieRaw = prod.optString("categories", "");

                if (categorieRaw != null && !categorieRaw.isEmpty()) {
                    String[] listaCategorie = categorieRaw.split(",");

                    // Seleziona l'ultima categoria perché solitamente è la più specifica
                    String categoriaPulita = listaCategorie[listaCategorie.length - 1];

                    info.categoria = categoriaPulita.trim();
                } else {
                    info.categoria = "";
                }

                info.immagine = prod.optString("image_url", "");
                info.barcode = barcode;
                return info;
            } else {
                return null; // Prodotto non trovato nel database OpenFoodFacts
            }
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    // DTO interno per passare i dati grezzi al Controller
    public static class ProdottoInfo {
        public String nome;
        public String marca;
        public String categoria;
        public String immagine;
        public String barcode;
    }
}