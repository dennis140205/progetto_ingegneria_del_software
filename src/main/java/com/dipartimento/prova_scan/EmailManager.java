package com.dipartimento.prova_scan;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailManager {

    public static void inviaEmailScadenza(String messaggioProdotti) {
        // 1. Carica le proprietà interne (che ora includono anche user/pass)
        Properties smtpProps = ConfigManager.getSmtpProperties();

        // 2. Ottieni l'email del destinatario (quella rimane scelta dall'utente)
        final String toEmail = ConfigManager.getUserEmail();

        // --- MODIFICA: Prendi le credenziali dal file interno ---
        final String username = smtpProps.getProperty("mail.username");
        final String password = smtpProps.getProperty("mail.password");
        // --------------------------------------------------------

        // Controllo di sicurezza
        if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
            System.err.println("Invio email fallito: 'Email Destinatario' non è impostata.");
            return;
        }

        // Passa le smtpProps per la sessione
        Session session = Session.getInstance(smtpProps, new Authenticator() {
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