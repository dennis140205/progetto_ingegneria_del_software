package com.dipartimento.prova_scan;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Gestisce la logica di invio delle email di notifica.
 */
public class EmailManager {

    /**
     * Invia un'email di avviso scadenza.
     * @param messaggioProdotti L'elenco dei prodotti da includere nel corpo dell'email.
     */
    public static void inviaEmailScadenza(String messaggioProdotti) {
        Properties prop = ConfigManager.getProperties();

        final String username = prop.getProperty("mail.username");
        final String password = prop.getProperty("mail.password");
        final String toEmail = prop.getProperty("mail.to");

        // Verifica se le proprietà essenziali sono caricate
        if (username == null || password == null || toEmail == null) {
            System.err.println("Errore: Controlla che mail.username, mail.password e mail.to siano impostati in email.properties.");
            return;
        }

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Avviso Scadenza Prodotti");

            String corpoEmail = "Attenzione! I seguenti prodotti sono in scadenza:\n\n"
                    + messaggioProdotti;
            message.setText(corpoEmail);

            Transport.send(message);
            System.out.println("Email di avviso inviata con successo a " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Errore durante l'invio dell'email:");
            e.printStackTrace();
        }
    }
}