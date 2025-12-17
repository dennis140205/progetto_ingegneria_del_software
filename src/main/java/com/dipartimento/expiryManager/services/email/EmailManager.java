package com.dipartimento.expiryManager.services.email;

import com.dipartimento.expiryManager.util.ConfigManager;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

// Gestisce l'invio delle email di notifica utilizzando il protocollo SMTP.
public class EmailManager {

    public static void inviaEmailScadenza(String messaggioProdotti) {
        // Recupera le configurazioni dal ConfigManager (credenziali SMTP e email utente)
        Properties smtpProps = ConfigManager.getSmtpProperties();
        String toEmail = ConfigManager.getUserEmail();

        String username = smtpProps.getProperty("mail.username");
        String password = smtpProps.getProperty("mail.password");

        // Validazione base del destinatario
        if (toEmail == null || !toEmail.matches(".+@.+\\..+")) {
            System.err.println("Email destinatario non valida.");
            return;
        }

        // Verifica presenza credenziali per l'autenticazione
        if (username == null || password == null) {
            System.err.println("Credenziali SMTP mancanti.");
            return;
        }

        // Creazione sessione di posta con autenticazione
        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Costruzione del messaggio MIME
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Avviso Scadenza Prodotti");

            // Impostazione del corpo del testo
            message.setText("Attenzione!\n\nI seguenti prodotti sono in scadenza:\n\n" + messaggioProdotti);

            // Invio effettivo del messaggio
            Transport.send(message);
            System.out.println("Email inviata correttamente a " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Errore durante l'invio dell'email:");
            e.printStackTrace();
        }
    }
}