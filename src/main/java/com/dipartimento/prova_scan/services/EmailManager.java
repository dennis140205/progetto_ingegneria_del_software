package com.dipartimento.prova_scan.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailManager {

    public static void inviaEmailScadenza(String messaggioProdotti) {
        Properties smtpProps = ConfigManager.getSmtpProperties();
        String toEmail = ConfigManager.getUserEmail();

        String username = smtpProps.getProperty("mail.username");
        String password = smtpProps.getProperty("mail.password");

        if (toEmail == null || !toEmail.contains("@")) {
            System.err.println("Email destinatario non valida.");
            return;
        }

        if (username == null || password == null) {
            System.err.println("Credenziali SMTP mancanti.");
            return;
        }

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

            message.setText("Attenzione!\n\nI seguenti prodotti sono in scadenza:\n\n" + messaggioProdotti);

            Transport.send(message);
            System.out.println("Email inviata correttamente a " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Errore durante l'invio dell'email:");
            e.printStackTrace();
        }
    }
}