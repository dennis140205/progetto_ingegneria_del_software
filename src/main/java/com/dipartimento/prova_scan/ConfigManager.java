package com.dipartimento.prova_scan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

public class ConfigManager {

    // File per le impostazioni SMTP (dentro il JAR)
    private static final Properties smtpProperties = new Properties();

    // File per le impostazioni utente (nella home dell'utente)
    private static final Properties userProperties = new Properties();
    private static final String CONFIG_FILE_NAME = "gestione_scadenze_config.properties";
    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME;

    static {
        // 1. Carica le impostazioni SMTP (sola lettura)
        try (InputStream input = ConfigManager.class.getResourceAsStream("/email.properties")) {
            if (input == null) {
                System.err.println("Errore: Impossibile trovare email.properties");
            } else {
                smtpProperties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Carica le impostazioni utente (leggibili e scrivibili)
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            userProperties.load(fis);
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File di configurazione utente non trovato, ne verrà creato uno nuovo.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Restituisce le proprietà SMTP (host, port, auth...).
     */
    public static Properties getSmtpProperties() {
        return smtpProperties;
    }

    // --- METODI AGGIORNATI ---

    /**
     * Restituisce un oggetto Properties con le credenziali utente (mittente, pass, destinatario).
     */
    public static Properties getUserProperties() {
        return userProperties;
    }

    /**
     * Salva tutte le impostazioni utente nel file esterno.
     */
    public static void saveUserSettings(String username, String password, String toEmail) {
        userProperties.setProperty("mail.username", username);
        userProperties.setProperty("mail.password", password);
        userProperties.setProperty("mail.to", toEmail);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE_PATH)) {
            userProperties.store(fos, "Configurazione Utente - Gestione Scadenze (mail.password è la Password per App)");
            System.out.println("Impostazioni utente salvate in: " + CONFIG_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}