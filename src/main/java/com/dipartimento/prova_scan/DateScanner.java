package com.dipartimento.prova_scan;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sourceforge.tess4j.Tesseract;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.function.Consumer;

public class DateScanner {
    private volatile boolean running = true;
    private String resultText = null;

    /**
     * Avvia lo scanner OCR per la data.
     * @param parentStage La finestra "proprietaria" (es. AggiungiProdotto) che verrà bloccata.
     * @param onDateFound Il Consumer che riceverà la data (o null se annullato).
     */
    public void start(Stage parentStage, Consumer<String> onDateFound) {
        Stage stage = new Stage();
        stage.setTitle("Scansione Data Scadenza");

        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);

        // --- MODIFICATO ---
        Label overlay = new Label("Inquadra la data (GG/MM/AAAA o GG/MM/AA)");
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 16px;");
        // --- FINE MODIFICA ---

        Rectangle focusRect = new Rectangle(400, 150);
        focusRect.setStroke(Color.RED);
        focusRect.setStrokeWidth(3);
        focusRect.setFill(Color.TRANSPARENT);

        StackPane root = new StackPane(imageView, focusRect, overlay);
        StackPane.setAlignment(overlay, Pos.TOP_CENTER);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();

        new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                System.err.println("Nessuna webcam trovata!");
                return;
            }

            Dimension[] sizes = webcam.getViewSizes();
            Dimension best = Arrays.stream(sizes)
                    .max((d1,d2) -> Integer.compare(d1.width*d1.height, d2.width*d2.height))
                    .orElse(new Dimension(640,480));
            webcam.setViewSize(best);
            webcam.open();

            Tesseract tess = new Tesseract();
            tess.setDatapath("tessdata");
            tess.setLanguage("ita");
            tess.setTessVariable("tessedit_char_whitelist", "0123456789/");

            while (running && resultText == null) {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {

                    int rectWidth = frame.getWidth()/2;
                    int rectHeight = frame.getHeight()/3;
                    int startX = frame.getWidth()/4;
                    int startY = frame.getHeight()/3;

                    BufferedImage cropped;
                    try {
                        cropped = frame.getSubimage(startX, startY, rectWidth, rectHeight);
                    } catch (Exception e) {
                        System.err.println("Errore ritaglio: " + e.getMessage());
                        continue;
                    }

                    Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(cropped, null)));

                    try {
                        String ocr = tess.doOCR(cropped)
                                .replace("\n", " ")
                                .replaceAll("\\s+", "");

                        // --- MODIFICATO ---
                        // Cerca il pattern GG/MM/AAAA oppure GG/MM/AA
                        // (Diamo priorità al formato a 4 cifre)
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d{2}/\\d{2}/\\d{4}|\\d{2}/\\d{2}/\\d{2})");
                        // --- FINE MODIFICA ---

                        java.util.regex.Matcher matcher = pattern.matcher(ocr);

                        if (matcher.find()) {
                            resultText = matcher.group(1); // Data trovata!
                            running = false;

                            Platform.runLater(() -> {
                                overlay.setText("Data rilevata: " + resultText);
                                stage.close();
                            });
                        }
                    } catch (Exception e) {
                        // Ignora errori OCR
                    }
                }
                try {
                    Thread.sleep(250); // Pausa per non sovraccaricare la CPU
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            webcam.close();
        }).start();

        stage.setOnHidden(e -> {
            onDateFound.accept(resultText); // Invia il risultato (data o null)
        });

        stage.setOnCloseRequest(e -> {
            running = false; // Ferma il thread
        });
    }
}