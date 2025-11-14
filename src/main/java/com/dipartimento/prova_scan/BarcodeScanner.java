package com.dipartimento.prova_scan;

import com.github.sarxos.webcam.Webcam;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer; // Import necessario

public class BarcodeScanner {
    private volatile boolean running = true;
    private String resultText = null;

    /**
     * Avvia lo scanner.
     * @param parentStage La finestra "proprietaria" (es. la Home) che verrà bloccata.
     * @param onScan Il Consumer che riceverà il codice (o null se annullato).
     */
    public void startScanner(Stage parentStage, Consumer<String> onScan) {
        Stage stage = new Stage();
        stage.setTitle("Scansione Barcode");

        // Imposta la finestra Home come proprietario e bloccala
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);

        Label overlay = new Label("Inquadra il codice a barre nell'area centrale");
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 16px;");

        // Rettangolo centrale guida
        Rectangle focusRect = new Rectangle(400, 150);
        focusRect.setStroke(Color.RED);
        focusRect.setStrokeWidth(3);
        focusRect.setFill(Color.TRANSPARENT);

        StackPane root = new StackPane(imageView, focusRect, overlay);
        StackPane.setAlignment(overlay, Pos.TOP_CENTER);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show(); // Mostra la finestra modale

        new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                System.err.println("Nessuna webcam trovata!");
                return;
            }

            // Risoluzione massima disponibile
            Dimension[] sizes = webcam.getViewSizes();
            Dimension best = Arrays.stream(sizes)
                    .max((d1,d2) -> Integer.compare(d1.width*d1.height, d2.width*d2.height))
                    .orElse(new Dimension(640,480));
            webcam.setViewSize(best);
            webcam.open();

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.EAN_13,
                    BarcodeFormat.CODE_128,
                    BarcodeFormat.UPC_A,
                    BarcodeFormat.CODE_39,
                    BarcodeFormat.QR_CODE
            ));

            while (running && resultText == null) {
                BufferedImage frame = webcam.getImage();
                if (frame != null) {
                    // Ritaglio area centrale
                    int rectWidth = frame.getWidth()/2;
                    int rectHeight = frame.getHeight()/3;
                    int startX = frame.getWidth()/4;
                    int startY = frame.getHeight()/3;
                    BufferedImage cropped = frame.getSubimage(startX, startY, rectWidth, rectHeight);

                    Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(cropped, null)));

                    LuminanceSource source = new BufferedImageLuminanceSource(cropped);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    try {
                        Result result = new MultiFormatReader().decode(bitmap, hints);
                        resultText = result.getText();
                        running = false;

                        // Codice rilevato, chiudi la finestra (attiverà l'onCloseRequest)
                        Platform.runLater(() -> {
                            overlay.setText("Codice rilevato: " + resultText);
                            stage.close();
                        });

                    } catch (NotFoundException e) {
                        // Nessun barcode trovato nel frame
                    }
                }

                try {
                    Thread.sleep(100); // pausa tra i frame
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            webcam.close();
        }).start();

        // Gestore della chiusura: questo è l'UNICO punto che chiama il callback
        stage.setOnCloseRequest(e -> {
            running = false; // Ferma il thread
            onScan.accept(resultText); // Invia il risultato (codice o null)
        });
    }
}