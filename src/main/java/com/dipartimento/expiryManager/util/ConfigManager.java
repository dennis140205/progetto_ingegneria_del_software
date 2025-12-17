package com.dipartimento.expiryManager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

// Gestisce le configurazioni dell'applicazione
public class ConfigManager {

    private static final Properties smtpProperties = new Properties();
    private static final Properties userProperties = new Properties();
    private static final String CONFIG_FILE_NAME = "gestione_scadenze_config.properties";
    // Percorso del file di configurazione nella directory home dell'utente
    private static final String CONFIG_FILE_PATH = System.getProperty("user.home") + File.separator + CONFIG_FILE_NAME;

    static {
        // Carica le impostazioni SMTP dal file incluso nel JAR (risorse interne)
        try (InputStream input = ConfigManager.class.getResourceAsStream("/config/email.properties")) {
            if (input == null) {
                System.err.println("Errore: Impossibile trovare email.properties");
            } else {
                smtpProperties.load(input);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Carica le preferenze utente dal file esterno su disco
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH)) {
            userProperties.load(fis);
        } catch (java.io.FileNotFoundException e) {
            // File non esistente, normale al primo avvio
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Restituisce una copia delle proprietà per evitare modifiche accidentali
    public static Properties getSmtpProperties() {
        Properties copy = new Properties();
        copy.putAll(smtpProperties);
        return copy;
    }

    public static Properties getUserProperties() {
        Properties copy = new Properties();
        copy.putAll(userProperties);
        return copy;
    }

    // Recupera l'email destinatario dalle preferenze caricate
    public static String getUserEmail() {
        return userProperties.getProperty("mail.to", "");
    }

    // Salva l'email del destinatario nel file di configurazione persistente sul disco
    public static void saveUserEmail(String toEmail) {
        userProperties.setProperty("mail.to", toEmail);
        File configFile = new File(CONFIG_FILE_PATH);
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            userProperties.store(fos, "Configurazione Utente - Solo Destinatario");
            System.out.println("Email destinatario salvata in: " + CONFIG_FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}