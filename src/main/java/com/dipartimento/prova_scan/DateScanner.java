package com.dipartimento.prova_scan;

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

public class DateScanner {
    private volatile boolean running = true;

    // Flag per evitare di lanciare troppi processi OCR insieme
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

        // 1. EFFETTO SPECCHIO
        imageView.setScaleX(-1);

        Label overlay = new Label("Inquadra la data");
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 16px;");

        StackPane root = new StackPane(imageView, overlay);
        StackPane.setAlignment(overlay, Pos.TOP_CENTER);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        // Thread Principale: Gestisce SOLO la Webcam (Fluidità massima)
        new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                Platform.runLater(() -> overlay.setText("Nessuna webcam trovata!"));
                return;
            }

            // Risoluzione media per bilanciare velocità OCR e qualità video
            Dimension[] sizes = webcam.getViewSizes();
            Dimension best = Arrays.stream(sizes)
                    .max((d1,d2) -> Integer.compare(d1.width*d1.height, d2.width*d2.height))
                    .orElse(new Dimension(640,480));
            webcam.setViewSize(best);

            if (!webcam.open()) return;

            // Configurazione Tesseract (fuori dal loop)
            Tesseract tess = new Tesseract();
            tess.setDatapath("tessdata");
            tess.setLanguage("ita");
            // Whitelist: ammettiamo numeri, /, -, . e spazi
            tess.setTessVariable("tessedit_char_whitelist", "0123456789/-. ");

            Pattern datePattern = Pattern.compile("(\\d{2}[/.-]\\d{2}[/.-]\\d{2,4})");

            while (running && resultText == null) {
                // 1. Cattura Immagine
                BufferedImage frame = webcam.getImage();

                if (frame != null) {
                    // 2. Aggiorna Video (IMMEDIATO)
                    Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(frame, null)));

                    // 3. Lancia OCR in Background (SOLO SE non sta già lavorando)
                    if (!isProcessingOCR.get()) {
                        isProcessingOCR.set(true); // Occupiamo il "semaforo"

                        // Creiamo un thread usa-e-getta per l'analisi di QUESTO frame
                        new Thread(() -> {
                            try {
                                // Analizziamo l'immagine originale (NON specchiata)
                                String ocrResult = tess.doOCR(frame);

                                // Pulizia risultato
                                ocrResult = ocrResult.replaceAll("\n", " ").replaceAll("\\s+", "");

                                Matcher matcher = datePattern.matcher(ocrResult);
                                if (matcher.find()) {
                                    resultText = matcher.group(1);
                                    running = false; // Ferma il loop principale

                                    Platform.runLater(() -> {
                                        overlay.setText("Data rilevata: " + resultText);
                                        stage.close();
                                    });
                                }
                            } catch (TesseractException e) {
                                // Ignora errori OCR
                            } finally {
                                // Finito il lavoro, liberiamo il semaforo per il prossimo frame disponibile
                                isProcessingOCR.set(false);
                            }
                        }).start();
                    }
                }

                // Piccola pausa per la webcam (standard 30 fps circa)
                try { Thread.sleep(30); } catch (Exception e) {}
            }

            webcam.close();
        }).start();

        stage.setOnCloseRequest(e -> running = false);
        stage.setOnHidden(e -> onDateFound.accept(resultText));
    }
}