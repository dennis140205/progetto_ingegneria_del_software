package com.dipartimento.prova_scan.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    private static final Properties smtpProperties = new Properties();
    private static final Properties userProperties = new Properties();
    private static final String CONFIG_FILE_NAME = "gestione_scadenze_config.properties";
    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME;

    static {
        // Carica email.properties (che ora ha le credenziali hardcoded)
        try (InputStream input = ConfigManager.class.getResourceAsStream("/email.properties")) {
            if (input == null) {
                System.err.println("Errore: Impossibile trovare email.properties");
            } else {
                smtpProperties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Carica user config (solo per il destinatario)
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            userProperties.load(fis);
        } catch (java.io.FileNotFoundException e) {
            // File non esistente, normale al primo avvio
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Properties getSmtpProperties() {
        return smtpProperties;
    }

    public static Properties getUserProperties() {
        return userProperties;
    }

    public static String getUserEmail() {
        return userProperties.getProperty("mail.to", "");
    }

    /**
     * --- NUOVO METODO ---
     * Salva SOLO l'email del destinatario nel file esterno.
     */
    public static void saveUserEmail(String toEmail) {
        userProperties.setProperty("mail.to", toEmail);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE_PATH)) {
            userProperties.store(fos, "Configurazione Utente - Solo Destinatario");
            System.out.println("Email destinatario salvata in: " + CONFIG_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}