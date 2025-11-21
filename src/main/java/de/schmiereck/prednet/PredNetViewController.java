package de.schmiereck.prednet;

import de.schmiereck.prednet.service.CurveDto;
import de.schmiereck.prednet.service.CurveGeneratorService;
import de.schmiereck.prednet.service.PredNetManagerService;
import de.schmiereck.prednet.service.PredNetManagerServiceFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;
import javafx.scene.shape.Line;

public class PredNetViewController {
    @FXML
    private Pane chartPane;

    private PredNetManagerService predNetManagerService;
    private Polyline inputLine;
    private Polyline outputLine;
    private Timeline timeline;
    private Line zeroLine;

    public void init(final PredNetManagerService predNetManagerService) {
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigSawtooth;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigSlowSine;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallSlowSine;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigFastSine;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.ModulatedSine;

        this.predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();

        this.predNetManagerService.initNet(curveType);

        this.setupChart();
        this.startUpdates();
    }

    private void setupChart() {
        this.inputLine = new Polyline();
        this.inputLine.setStroke(Color.LIMEGREEN);
        this.inputLine.setStrokeWidth(8.0);

        this.outputLine = new Polyline();
        this.outputLine.setStroke(Color.BLUEVIOLET);
        this.outputLine.setStrokeWidth(4.0);

        this.zeroLine = new Line();
        this.zeroLine.setStroke(Color.WHITE);
        this.zeroLine.setStrokeWidth(2.0);

        this.chartPane.getChildren().addAll(zeroLine, inputLine, outputLine);
    }

    private void startUpdates() {
        this.timeline = new Timeline(new KeyFrame(Duration.millis(50), e -> updateCurves()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        this.timeline.play();
    }

    private void updateCurves() {
        this.predNetManagerService.runCalc(); // Berechnung anstoßen (alternativ getrennt, hier einfach integriert)
        final CurveDto curveDto = this.predNetManagerService.retrieveCurve();
        long[] inputArr = curveDto.getInputArr();
        long[] outputHistArr = curveDto.getOutputHistorieArr();

        this.inputLine.getPoints().clear();
        this.outputLine.getPoints().clear();

        double w = this.chartPane.getWidth();
        double h = this.chartPane.getHeight();
        if (w <= 0) w = 600;
        if (h <= 0) h = 300;

        double maxVal = 100.0; // bekannte Maximalwerte (Annahme)
        double minVal = -100.0; // bekannte Minimalwerte (Annahme)
        double range = maxVal - minVal;

        double yZero = h - ((0.0 - minVal) / range) * (h - 20) - 10; // Null-Linie Y berechnet
        this.zeroLine.setStartX(0);
        this.zeroLine.setEndX(w);
        this.zeroLine.setStartY(yZero);
        this.zeroLine.setEndY(yZero);

        // wie viele Schritte in die Zukunft vorhergesagt wird
        final int predictionCount = this.predNetManagerService.retrieveNetOutputCurveLength();

        int inputLength = inputArr.length;
        int outputLength = outputHistArr.length;
        //double dx = w / ((inputLength - 1) + predictionCount);
        double dx = w / ((outputLength - 1));

        for (int inputCurvePos = 0; inputCurvePos < inputLength; inputCurvePos++) {
            double xInput = inputCurvePos * dx;
            double yInput = h - ((inputArr[inputCurvePos] - minVal) / range) * (h - 20) - 10; // Padding 10
            this.inputLine.getPoints().addAll(xInput, yInput);
        }
        for (int outputCurvePos = 0; outputCurvePos < outputLength; outputCurvePos++) {
            double xOutput = (outputCurvePos) * dx; // Output ist eine Vorhersage in die Zukunft.
            double yOutput = h - ((outputHistArr[outputCurvePos] - minVal) / range) * (h - 20) - 10;
            this.outputLine.getPoints().addAll(xOutput, yOutput);
        }
    }
}
