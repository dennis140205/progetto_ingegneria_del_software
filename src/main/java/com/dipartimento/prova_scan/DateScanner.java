package com.dipartimento.prova_scan;

import com.github.sarxos.webcam.Webcam;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.sourceforge.tess4j.*;

import java.awt.image.BufferedImage;

public class DateScanner {

    private volatile boolean running = true;

    public void start(Stage parentStage, java.util.function.Consumer<String> onDateFound) {
        Stage stage = new Stage();
        ImageView imgView = new ImageView();
        imgView.setFitWidth(800);
        imgView.setFitHeight(600);
        Label overlay = new Label("Inquadra la data di scadenza");

        StackPane root = new StackPane(imgView, overlay);
        stage.setScene(new Scene(root, 800, 600));
        stage.show();

        new Thread(() -> {
            Webcam cam = Webcam.getDefault();
            cam.open();

            Tesseract tess = new Tesseract();
            tess.setDatapath("tessdata");
            tess.setLanguage("ita");

            while (running) {
                BufferedImage frame = cam.getImage();

                if (frame != null) {
                    Platform.runLater(() -> imgView.setImage(SwingFXUtils.toFXImage(frame, null)));

                    try {
                        String ocr = tess.doOCR(frame)
                                .replace("\n", " ")
                                .replace(" ", "")
                                .trim();

                        if (ocr.matches(".*\\d{2}/\\d{2}/\\d{4}.*")) {
                            running = false;
                            String date = ocr.replaceAll(".*?(\\d{2}/\\d{2}/\\d{4}).*", "$1");

                            Platform.runLater(() -> {
                                stage.close();
                                onDateFound.accept(date);
                            });
                        }

                    } catch (Exception ignored) {}
                }

                try {
                    Thread.sleep(150);
                } catch (Exception ignored) {}
            }

            cam.close();
        }).start();

        stage.setOnCloseRequest(e -> running = false);
    }
}
