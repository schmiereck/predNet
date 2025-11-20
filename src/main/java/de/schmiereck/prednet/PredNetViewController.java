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
        inputLine.setStrokeWidth(8.0);

        outputLine = new Polyline();
        outputLine.setStroke(Color.BLUEVIOLET);
        outputLine.setStrokeWidth(4.0);

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
        //double dx = w / (len - 1);
        double dx = w / (len);
        double maxVal = 100.0; // bekannte Maximalwerte (Annahme)
        double minVal = -100.0; // bekannte Minimalwerte (Annahme)
        double range = maxVal - minVal;

        for (int curvePos = 0; curvePos < len; curvePos++) {
            double xInput = curvePos * dx;
            double xOutput = (curvePos + 1) * dx; // Output ist eine Vorhersage in die zukunft.
            double yInput = h - ((inputArr[curvePos] - minVal) / range) * (h - 20) - 10; // Padding 10
            double yOutput = h - ((outputHistArr[curvePos] - minVal) / range) * (h - 20) - 10;
            inputLine.getPoints().addAll(xInput, yInput);
            outputLine.getPoints().addAll(xOutput, yOutput);
        }
    }
}
