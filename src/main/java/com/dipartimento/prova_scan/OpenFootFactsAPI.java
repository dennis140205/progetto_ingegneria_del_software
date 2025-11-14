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
                info.categoria = prod.optString("categories_tags", "");
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

