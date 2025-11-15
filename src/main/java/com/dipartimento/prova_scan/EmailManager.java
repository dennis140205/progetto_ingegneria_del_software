package com.dipartimento.prova_scan;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailManager {

    public static void inviaEmailScadenza(String messaggioProdotti) {
        // --- INIZIO MODIFICA ---
        // 1. Ottieni le proprietà SMTP (host, auth, etc.)
        Properties smtpProps = ConfigManager.getSmtpProperties();

        // 2. Ottieni le credenziali e il destinatario dal file di config utente
        Properties userProps = ConfigManager.getUserProperties();
        final String username = userProps.getProperty("mail.username");
        final String password = userProps.getProperty("mail.password");
        final String toEmail = userProps.getProperty("mail.to");
        // --- FINE MODIFICA ---

        // Controllo di sicurezza
        if (toEmail == null || toEmail.isEmpty() || !toEmail.contains("@")) {
            System.err.println("Invio email fallito: 'mail.to' non è impostata. (Impostala dal programma)");
            return;
        }
        if (username == null || username.isEmpty() || !username.contains("@")) {
            System.err.println("Invio email fallito: 'mail.username' (mittente) non è impostato.");
            return;
        }
        if (password == null || password.isEmpty()) {
            System.err.println("Invio email fallito: 'mail.password' (password app) non è impostata.");
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