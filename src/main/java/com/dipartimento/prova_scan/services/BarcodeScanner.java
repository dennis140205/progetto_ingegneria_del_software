package com.dipartimento.prova_scan.services;

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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BarcodeScanner {
    private volatile boolean running = true;
    private String resultText = null;

    public void startScanner(Stage parentStage, Consumer<String> onScan) {
        Stage stage = new Stage();
        stage.setTitle("Scansione Barcode");
        stage.initOwner(parentStage);
        stage.initModality(Modality.APPLICATION_MODAL);

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(800);
        imageView.setFitHeight(600);

        // Effetto specchio (ribalta l'immagine orizzontalmente)
        imageView.setScaleX(-1);

        Label overlay = new Label("Inquadra il codice a barre");
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-text-fill: white; -fx-padding: 8; -fx-font-size: 16px;");

        // --- MODIFICA: RETTANGOLO RIMOSSO ---
        // StackPane contiene solo l'immagine e l'etichetta di testo
        StackPane root = new StackPane(imageView, overlay);
        StackPane.setAlignment(overlay, Pos.TOP_CENTER);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();

        new Thread(() -> {
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                System.err.println("ERRORE: Nessuna webcam trovata!");
                Platform.runLater(() -> overlay.setText("Nessuna webcam trovata!"));
                return;
            }

            Dimension[] sizes = webcam.getViewSizes();
            Dimension best = Arrays.stream(sizes)
                    .max((d1,d2) -> Integer.compare(d1.width*d1.height, d2.width*d2.height))
                    .orElse(new Dimension(640,480));
            webcam.setViewSize(best);

            if (!webcam.open()) {
                System.err.println("ERRORE: Impossibile aprire la webcam!");
                return;
            }

            Map<DecodeHintType, Object> hints = new HashMap<>();
            hints.put(DecodeHintType.POSSIBLE_FORMATS, Arrays.asList(
                    BarcodeFormat.EAN_13, BarcodeFormat.CODE_128,
                    BarcodeFormat.UPC_A, BarcodeFormat.CODE_39, BarcodeFormat.QR_CODE
            ));
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

            while (running && resultText == null) {
                BufferedImage frame = webcam.getImage();

                if (frame != null) {
                    Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(frame, null)));

                    LuminanceSource source = new BufferedImageLuminanceSource(frame);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    try {
                        Result result = new MultiFormatReader().decode(bitmap, hints);
                        resultText = result.getText();
                        running = false;

                        Platform.runLater(() -> {
                            overlay.setText("Trovato: " + resultText);
                            stage.close();
                        });

                    } catch (NotFoundException e) {
                        // Nessun codice trovato
                    }
                }

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            webcam.close();
        }).start();

        stage.setOnCloseRequest(e -> running = false);
        stage.setOnHidden(e -> onScan.accept(resultText));
    }
}