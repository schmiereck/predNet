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
    private Line startTrainLine;
    private Line endTrainLine;

    public void init(final PredNetManagerService predNetManagerService) {
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigSawtooth;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigSlowSine;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallSlowSine;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigFastSine;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
        //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.ModulatedSine;
        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.Modulated2Sine;

        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;

        this.predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();

        this.predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength);

        this.setupChart();

        final double updateEveryMillis = 50.0D;
        this.startUpdates(updateEveryMillis);
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

        this.startTrainLine = new Line();
        this.startTrainLine.setStroke(Color.DARKGREEN);
        this.startTrainLine.setStrokeWidth(2.0);

        this.endTrainLine = new Line();
        this.endTrainLine.setStroke(Color.DARKGREEN);
        this.endTrainLine.setStrokeWidth(2.0);

        this.chartPane.getChildren().addAll(this.startTrainLine, this.endTrainLine, this.zeroLine, this.inputLine, this.outputLine);
    }

    private void startUpdates(final double updateEveryMillis) {
        this.timeline = new Timeline(new KeyFrame(Duration.millis(updateEveryMillis), e -> this.updateCurves()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        this.timeline.play();
    }

    private void updateCurves() {
        this.predNetManagerService.runCalc(); // Berechnung anstoßen (alternativ getrennt, hier einfach integriert)
        final CurveDto curveDto = this.predNetManagerService.retrieveCurve();
        final long[] inputArr = curveDto.inputCurveArr();
        final long[] outputHistArr = curveDto.outputHistorieCurveArr();

        this.inputLine.getPoints().clear();
        this.outputLine.getPoints().clear();

        final double width;
        final double height;
        if (this.chartPane.getWidth() <= 0.0D) {
            width = 600.0D;
        } else {
            width = this.chartPane.getWidth();
        }
        if (this.chartPane.getHeight() <= 0.0D) {
            height = 300.0D;
        } else {
            height = this.chartPane.getHeight();
        }

        final double maxVal = 100.0D; // bekannte Maximalwerte (Annahme)
        final double minVal = -100.0D; // bekannte Minimalwerte (Annahme)
        final double range = maxVal - minVal;

        final int inputLength = inputArr.length;
        final int outputLength = outputHistArr.length;
        //double dx = width / ((inputLength - 1) + predictionCount);
        final double dx = width / ((outputLength - 1));

        // Null-Linie Y berechnet:
        final double yZero = height - ((0.0D - minVal) / range) * (height - 20.0D) - 10.0D;

        this.zeroLine.setStartX(0);
        this.zeroLine.setStartY(yZero);
        this.zeroLine.setEndX(width);
        this.zeroLine.setEndY(yZero);

        final double maxYPos = calcYPos(height, minVal, range, maxVal);
        final double minYPos = calcYPos(height, minVal, range, minVal);
        final int netInputCurveLength = curveDto.netInputCurveLength();
        final int netOutputCurveLength = curveDto.netOutputCurveLength();
        final double startTrainXPos = width - (((netOutputCurveLength)) * dx);
        final double endTrainXPos = width - (((netInputCurveLength) + (netOutputCurveLength)) * dx);

        this.startTrainLine.setStartX(startTrainXPos);
        this.startTrainLine.setStartY(maxYPos);
        this.startTrainLine.setEndX(startTrainXPos);
        this.startTrainLine.setEndY(minYPos);

        this.endTrainLine.setStartX(endTrainXPos);
        this.endTrainLine.setStartY(maxYPos);
        this.endTrainLine.setEndX(endTrainXPos);
        this.endTrainLine.setEndY(minYPos);

        // wie viele Schritte in die Zukunft vorhergesagt wird
        final int predictionCount = this.predNetManagerService.retrieveNetOutputCurveLength();

        for (int inputCurvePos = 0; inputCurvePos < inputLength; inputCurvePos++) {
            final double xInput = inputCurvePos * dx;
            //final double yInput = height - ((inputCurveArr[inputCurvePos] - minVal) / range) * (height - 20.0D) - 10.0D; // Padding 10
            final double yInput = calcYPos(height, minVal, range, inputArr[inputCurvePos]);
            this.inputLine.getPoints().addAll(xInput, yInput);
        }
        for (int outputCurvePos = 0; outputCurvePos < outputLength; outputCurvePos++) {
            final double xOutput = (outputCurvePos) * dx; // Output ist eine Vorhersage in die Zukunft.
            //final double yOutput = height - ((outputHistArr[outputCurvePos] - minVal) / range) * (height - 20.0D) - 10.0D;
            final double yOutput = calcYPos(height, minVal, range, outputHistArr[outputCurvePos]);
            this.outputLine.getPoints().addAll(xOutput, yOutput);
        }
    }

    private static double calcYPos(final double height, final double minVal, final double range, final long value) {
        return calcYPos(height, minVal, range, (double) value);
    }

    private static double calcYPos(final double height, final double minVal, final double range, final double value) {
        return height - ((value - minVal) / range) * (height - 20.0D) - 10.0D; // Padding 10
    }
}
