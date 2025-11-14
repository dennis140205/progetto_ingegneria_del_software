package com.dipartimento.prova_scan;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class NotificheManager {
    public static void controllaScadenze(List<Prodotto> prodotti) {
        LocalDate oggi = LocalDate.now();
        List<Prodotto> inScadenza = prodotti.stream()
                .filter(p -> !p.getDataScadenza().isBefore(oggi))
                .filter(p -> p.getDataScadenza().isBefore(oggi.plusDays(3)))
                .collect(Collectors.toList());

        if (!inScadenza.isEmpty()) {
            String msg = inScadenza.stream()
                    .map(p -> p.getNome() + " (" + p.getDataScadenza() + ")")
                    .collect(Collectors.joining("\n"));
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Prodotti in scadenza");
                alert.setHeaderText("Attenzione!");
                alert.setContentText(msg);
                alert.showAndWait();
            });
        }
    }
}

