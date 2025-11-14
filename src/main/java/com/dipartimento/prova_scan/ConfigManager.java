package com.dipartimento.prova_scan;

import java.io.InputStream;
import java.util.Properties;

/**
 * Classe helper per caricare il file email.properties dalle risorse.
 */
public class ConfigManager {
    private static final Properties properties = new Properties();

    static {
        // Carica il file di configurazione all'avvio
        try (InputStream input = ConfigManager.class.getResourceAsStream("/email.properties")) {
            if (input == null) {
                System.err.println("Errore: Impossibile trovare email.properties");
            } else {
                properties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}