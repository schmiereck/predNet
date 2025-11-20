package de.schmiereck.prednet;

import de.schmiereck.prednet.service.CurveDto;
import de.schmiereck.prednet.service.PredNetService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;

public class PredNetViewController {
    @FXML
    private Pane chartPane;

    private PredNetService predNetService;
    private Polyline inputLine;
    private Polyline outputLine;
    private Timeline timeline;

    public void init(PredNetService service) {
        this.predNetService = service;
        setupChart();
        startUpdates();
    }

    private void setupChart() {
        inputLine = new Polyline();
        inputLine.setStroke(Color.LIMEGREEN);
        inputLine.setStrokeWidth(2.0);

        outputLine = new Polyline();
        outputLine.setStroke(Color.ORANGERED);
        outputLine.setStrokeWidth(2.0);

        chartPane.getChildren().addAll(inputLine, outputLine);
    }

    private void startUpdates() {
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateCurves()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateCurves() {
        predNetService.calc(); // Berechnung anstoßen (alternativ getrennt, hier einfach integriert)
        CurveDto dto = predNetService.retrieveCurve();
        long[] inputArr = dto.getInputArr();
        long[] outputHistArr = dto.getOutputHistorieArr();

        inputLine.getPoints().clear();
        outputLine.getPoints().clear();

        double w = chartPane.getWidth();
        double h = chartPane.getHeight();
        if (w <= 0) w = 600;
        if (h <= 0) h = 300;

        int len = inputArr.length;
        double dx = w / (len - 1);
        double maxVal = 100.0; // bekannte Maximalwerte (Annahme)

        for (int i = 0; i < len; i++) {
            double x = i * dx;
            double yInput = h - (inputArr[i] / maxVal) * (h - 20) - 10; // Padding 10
            double yOutput = h - (outputHistArr[i] / maxVal) * (h - 20) - 10;
            inputLine.getPoints().addAll(x, yInput);
            outputLine.getPoints().addAll(x, yOutput);
        }
    }
}

