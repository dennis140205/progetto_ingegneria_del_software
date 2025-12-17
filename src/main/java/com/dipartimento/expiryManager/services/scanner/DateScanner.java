package com.dipartimento.expiryManager.services.scanner;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Utilizza OCR (Tesseract) per estrarre date di scadenza dalle immagini della webcam.
public class DateScanner {
    private volatile boolean running = true;

    // Flag per evitare di lanciare troppi processi OCR contemporaneamente, garantendo che l'interfaccia video rimanga fluida.
    private final AtomicBoolean isProcessingOCR = new AtomicBoolean(false);

    private String resultText = null;

    public void start(Stage parentStage, Consumer<String> onDateFound) {
        Stage stage = new Stage();
        stage.setTitle("Scansione Data Scadenza");
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);

        // Ribalta l'immagine per rendere l'inquadratura più naturale per l'utente (specchio)
        imageView.setScaleX(-1);

        Label overlay = new Label("Inquadra la data");
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 16px;");

        StackPane root = new StackPane(imageView, overlay);
        StackPane.setAlignment(overlay, Pos.TOP_CENTER);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Questo thread gestisce solo il flusso video della Webcam per la massima fluidità
        new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                Platform.runLater(() -> overlay.setText("Nessuna webcam trovata!"));
                return;
            }

            // Selezione risoluzione media per bilanciare velocità OCR e qualità video
            Dimension[] sizes = webcam.getViewSizes();
            Dimension best = Arrays.stream(sizes)
                    .max((d1,d2) -> Integer.compare(d1.width*d1.height, d2.width*d2.height))
                    .orElse(new Dimension(640,480));
            webcam.setViewSize(best);

            if (!webcam.open()) return;

            // Configurazione Tesseract (Motore OCR)
            Tesseract tess = new Tesseract();
            tess.setDatapath("src/main/resources/tessdata"); // Cartella contenente i file di lingua (.traineddata)
            tess.setLanguage("ita");

            // Whitelist: ottimizza l'OCR limitando i caratteri riconosciuti a numeri e separatori di data
            tess.setTessVariable("tessedit_char_whitelist", "0123456789/-. ");

            // Regex per trovare pattern di date (es. 13/06/2027 o 13-06-27 ecc.)
            Pattern datePattern = Pattern.compile("(\\d{2}[/.-]\\d{2}[/.-]\\d{2,4})");

            while (running && resultText == null) {
                // Cattura frame corrente
                BufferedImage frame = webcam.getImage();

                if (frame != null) {
                    // Aggiorna video
                    Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(frame, null)));

                    // Lancia OCR in Background (solo se non sta già lavorando)
                    // Questo evita che l'OCR (operazione lenta) blocchi il video
                    if (!isProcessingOCR.get()) {
                        isProcessingOCR.set(true); // Occupa il "semaforo"

                        // Thread per l'analisi OCR di questo specifico frame
                        new Thread(() -> {
                            try {
                                // Esegue l'OCR sull'immagine originale
                                String ocrResult = tess.doOCR(frame);

                                // Pulizia stringa risultato
                                ocrResult = ocrResult.replaceAll("\n", " ").replaceAll("\\s+", "");

                                // Verifica se il testo contiene una data valida
                                Matcher matcher = datePattern.matcher(ocrResult);
                                if (matcher.find()) {
                                    resultText = matcher.group(1);
                                    running = false; // Ferma il loop della webcam

                                    Platform.runLater(() -> {
                                        overlay.setText("Data rilevata: " + resultText);
                                        stage.close();
                                    });
                                }
                            } catch (TesseractException e) {
                                // Ignora errori OCR temporanei
                            } finally {
                                // Pronto per analizzare un nuovo frame
                                isProcessingOCR.set(false);
                            }
                        }).start();
                    }
                }

                // Piccola pausa per mantenere il framerate della webcam (30 fps)
                try { Thread.sleep(30); } catch (Exception e) {}
            }

            webcam.close();
        }).start();

        stage.setOnCloseRequest(e -> running = false);
        stage.setOnHidden(e -> onDateFound.accept(resultText));
    }
}