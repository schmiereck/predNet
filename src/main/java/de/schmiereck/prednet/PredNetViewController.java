package de.schmiereck.prednet;

import de.schmiereck.prednet.service.CurveDto;
import de.schmiereck.prednet.service.CurveGeneratorService;
import de.schmiereck.prednet.service.PredNetManagerService;
import de.schmiereck.prednet.service.PredNetManagerServiceFactory;
import de.schmiereck.prednet.service.baseNet.BaseNetService;
import de.schmiereck.prednet.service.normNet.NormNetService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import javafx.util.Duration;
import javafx.scene.shape.Line;

public class PredNetViewController {
    @FXML
    private Pane chartPane;
    @FXML
    private Button speed5Button;
    @FXML
    private Button speed10Button;
    @FXML
    private Button speed25Button;
    @FXML
    private Button speed50Button;
    @FXML
    private Button speed250Button;
    @FXML
    private Button speed500Button;

    private PredNetManagerService predNetManagerService;
    private Polyline expectedOutputHistorieLine;
    private Polyline expectedOutputLine;
    private Polyline outputLine;
    private Polyline outputHistorieLine;
    private Polyline extraLine;
    private Timeline timeline;
    private Line zeroLine;
    private Line startTrainLine;
    private Line endTrainLine;
    private static final double DEFAULT_UPDATE_INTERVAL = 25.0D;
    private double updateEveryMillis = DEFAULT_UPDATE_INTERVAL;

    public void init(final PredNetManagerService predNetManagerService) {
        final int testCase = 3;

        final CurveGeneratorService.CurveType curveType;
        final int netInputCurveLength;
        final int netOutputCurveLength;
        final int hiddenLayerCount;
        final int hiddenLayerNeuronCount;
        final boolean useOutputAsInput;
        final NormNetService.LoopbackType loopbackType;

        switch(testCase) {
            case 0 -> {
                // Very good:
                /*
                 */
                //curveType = CurveGeneratorService.CurveType.BigSawtooth;
                //curveType = CurveGeneratorService.CurveType.BigSlowSine;
                //curveType = CurveGeneratorService.CurveType.SmallSlowSine;
                curveType = CurveGeneratorService.CurveType.BigFastSine;
                //final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
                netInputCurveLength = 8;
                netOutputCurveLength = 6;
                hiddenLayerCount = 3;
                hiddenLayerNeuronCount = netInputCurveLength * 2;
                useOutputAsInput = false;
                loopbackType = NormNetService.LoopbackType.None;
            }
            case 1 -> {
                // Very good:
                curveType = CurveGeneratorService.CurveType.ModulatedSine;
                netInputCurveLength = 16;
                netOutputCurveLength = 6;
                hiddenLayerCount = 6;
                hiddenLayerNeuronCount = netInputCurveLength * 2;
                useOutputAsInput = true;
                loopbackType = NormNetService.LoopbackType.None;
            }
            case 2 -> {
                // Very difficult:
                curveType = CurveGeneratorService.CurveType.Modulated2Sine;
                netInputCurveLength = 16+8;
                netOutputCurveLength = 6;
                hiddenLayerCount = 6+3;
                hiddenLayerNeuronCount = netInputCurveLength * 2;
                useOutputAsInput = true;
                loopbackType = NormNetService.LoopbackType.None;
            }
            case 3 -> {
                // Very good with useOutputAsInput as Memory (Nearly impossible without):
                curveType = CurveGeneratorService.CurveType.SmallFastSine;
                netInputCurveLength = 1;  // Only 1 input value to predict the next 6 output values.
                netOutputCurveLength = 6;
                hiddenLayerCount = 3;
                hiddenLayerNeuronCount = 6;
                useOutputAsInput = true;    // But Output used as Input.
                loopbackType = NormNetService.LoopbackType.None;
            }
            case 4 -> {
                // Very good with useOutputAsInput as Memory (Nearly impossible without):
                curveType = CurveGeneratorService.CurveType.SmallFastSine;
                netInputCurveLength = 1;  // Only 1 input value to predict the next 6 output values.
                netOutputCurveLength = 6;
                hiddenLayerCount = 12;
                hiddenLayerNeuronCount = 12;
                useOutputAsInput = false;   // No Output used as Input.
                loopbackType = NormNetService.LoopbackType.None;
                //loopbackType = NormNetService.LoopbackType.Neuron;
                //loopbackType = NormNetService.LoopbackType.ParentNeuron;
            }
            default -> throw new IllegalArgumentException("Invalid test case");
        }

        this.predNetManagerService = predNetManagerService;

        this.predNetManagerService.initNet(curveType,
                netInputCurveLength, netOutputCurveLength, hiddenLayerCount, hiddenLayerNeuronCount,
                useOutputAsInput, loopbackType);

        this.setupChart();

        this.switchUpdateInterval(DEFAULT_UPDATE_INTERVAL, this.speed25Button);
    }

    private void setupChart() {
        this.expectedOutputHistorieLine = new Polyline();
        this.expectedOutputHistorieLine.setStroke(Color.LIMEGREEN);
        this.expectedOutputHistorieLine.setStrokeWidth(8.0);

        this.expectedOutputLine = new Polyline();
        this.expectedOutputLine.setStroke(Color.LIGHTGREEN);
        this.expectedOutputLine.setStrokeWidth(8.0);

        this.outputHistorieLine = new Polyline();
        this.outputHistorieLine.setStroke(Color.BLUEVIOLET);
        this.outputHistorieLine.setStrokeWidth(4.0);

        this.outputLine = new Polyline();
        this.outputLine.setStroke(Color.LIGHTBLUE);
        this.outputLine.setStrokeWidth(6.0);

        this.extraLine = new Polyline();
        this.extraLine.setStroke(Color.DARKGOLDENROD);
        this.extraLine.setStrokeWidth(12.0);

        this.zeroLine = new Line();
        this.zeroLine.setStroke(Color.WHITE);
        this.zeroLine.setStrokeWidth(2.0);

        this.startTrainLine = new Line();
        this.startTrainLine.setStroke(Color.DARKGREEN);
        this.startTrainLine.setStrokeWidth(2.0);

        this.endTrainLine = new Line();
        this.endTrainLine.setStroke(Color.DARKGREEN);
        this.endTrainLine.setStrokeWidth(6.0);

        this.chartPane.getChildren().addAll(this.startTrainLine, this.endTrainLine,
                this.zeroLine,
                this.extraLine,
                this.expectedOutputLine, this.expectedOutputHistorieLine,
                this.outputLine, this.outputHistorieLine);
    }

    private void startUpdates() {
        if (this.timeline != null) {
            this.timeline.stop();
        }
        this.timeline = new Timeline(new KeyFrame(Duration.millis(this.updateEveryMillis), e -> this.updateCurves()));
        this.timeline.setCycleCount(Timeline.INDEFINITE);
        this.timeline.play();
    }

    private void switchUpdateInterval(final double newIntervalMillis, final Button activeButton) {
        this.updateEveryMillis = newIntervalMillis;
        this.startUpdates();
        this.updateSpeedButtonsState(activeButton);
    }

    private void updateSpeedButtonsState(final Button activeButton) {
        final Button[] buttons = {this.speed5Button, this.speed10Button, this.speed25Button, this.speed50Button, this.speed250Button, this.speed500Button};
        for (final Button button : buttons) {
            if (button != null) {
                button.setDisable(button == activeButton);
            }
        }
    }

    private void updateCurves() {
        this.predNetManagerService.runCalc(); // Berechnung anstoßen (alternativ getrennt, hier einfach integriert)
        final CurveDto curveDto = this.predNetManagerService.retrieveCurve();

        final long[] expectedOutputHistorieArr = curveDto.expectedOutputHistorieArr();
        final long[] expectedOutputArr = curveDto.expectedOutputArr();

        final long[] outputHistorieCurveArr = curveDto.outputHistorieCurveArr();
        final long[] inputCurveArr = curveDto.inputCurveArr();
        final long[] outputCurveArr = curveDto.outputCurveArr();

        this.expectedOutputLine.getPoints().clear();
        this.expectedOutputHistorieLine.getPoints().clear();
        this.outputHistorieLine.getPoints().clear();
        this.outputLine.getPoints().clear();
        this.extraLine.getPoints().clear();

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

        final int curveLength = (expectedOutputHistorieArr.length + expectedOutputArr.length);
        final double dx = width / (curveLength - 0);

        // Null-Linie Y berechnet:
        final double yZero = height - ((0.0D - minVal) / range) * (height - 20.0D) - 10.0D;

        this.zeroLine.setStartX(0);
        this.zeroLine.setStartY(yZero);
        this.zeroLine.setEndX((curveLength - 1) * dx);
        this.zeroLine.setEndY(yZero);

        final double maxYPos = calcYPos(height, minVal, range, maxVal);
        final double minYPos = calcYPos(height, minVal, range, minVal);
        final int netInputCurveLength = curveDto.netInputCurveLength();
        final int netOutputCurveLength = curveDto.netOutputCurveLength();
        final double startTrainXPos = ((expectedOutputHistorieArr.length) - (netInputCurveLength)) * dx;
        final double endTrainXPos = ((expectedOutputHistorieArr.length - 1)) * dx;

        this.startTrainLine.setStartX(startTrainXPos);
        this.startTrainLine.setStartY(maxYPos);
        this.startTrainLine.setEndX(startTrainXPos);
        this.startTrainLine.setEndY(minYPos);

        this.endTrainLine.setStartX(endTrainXPos);
        this.endTrainLine.setStartY(maxYPos);
        this.endTrainLine.setEndX(endTrainXPos);
        this.endTrainLine.setEndY(minYPos);

        for (int inputCurvePos = 0; inputCurvePos < expectedOutputHistorieArr.length; inputCurvePos++) {
            final double xInput = inputCurvePos * dx;
            final double yInput = calcYPos(height, minVal, range, expectedOutputHistorieArr[inputCurvePos]);
            this.expectedOutputHistorieLine.getPoints().addAll(xInput, yInput);
        }
        for (int inputCurvePos = 0; inputCurvePos < expectedOutputArr.length; inputCurvePos++) {
            final double xInput = ((expectedOutputHistorieArr.length) + inputCurvePos) * dx;
            final double yInput = calcYPos(height, minVal, range, expectedOutputArr[inputCurvePos]);
            this.expectedOutputLine.getPoints().addAll(xInput, yInput);
        }

        for (int inputCurvePos = 0; inputCurvePos < inputCurveArr.length; inputCurvePos++) {
            final double xInput = ((expectedOutputHistorieArr.length - netInputCurveLength) + inputCurvePos) * dx;
            final double yInput = calcYPos(height, minVal, range, inputCurveArr[inputCurvePos]);
            this.extraLine.getPoints().addAll(xInput, yInput);
        }
        for (int outputCurvePos = 0; outputCurvePos < outputHistorieCurveArr.length; outputCurvePos++) {
            final double xOutput = (outputCurvePos) * dx; // Output ist eine Vorhersage in die Zukunft.
            final double yOutput = calcYPos(height, minVal, range, outputHistorieCurveArr[outputCurvePos]);
            this.outputHistorieLine.getPoints().addAll(xOutput, yOutput);
        }
        for (int outputCurvePos = 0; outputCurvePos < outputCurveArr.length; outputCurvePos++) {
            final double xOutput = (expectedOutputHistorieArr.length + outputCurvePos) * dx; // Output ist eine Vorhersage in die Zukunft.
            final double yOutput = calcYPos(height, minVal, range, outputCurveArr[outputCurvePos]);
            this.outputLine.getPoints().addAll(xOutput, yOutput);
        }
    }

    private static double calcYPos(final double height, final double minVal, final double range, final long value) {
        return calcYPos(height, minVal, range, (double) value);
    }

    private static double calcYPos(final double height, final double minVal, final double range, final double value) {
        return height - ((value - minVal) / range) * (height - 20.0D) - 10.0D; // Padding 10
    }

    @FXML
    private void handleSpeed5() {
        this.switchUpdateInterval(5.0D, this.speed5Button);
    }

    @FXML
    private void handleSpeed10() {
        this.switchUpdateInterval(10.0D, this.speed10Button);
    }

    @FXML
    private void handleSpeed25() {
        this.switchUpdateInterval(25.0D, this.speed25Button);
    }

    @FXML
    private void handleSpeed50() {
        this.switchUpdateInterval(50.0D, this.speed50Button);
    }

    @FXML
    private void handleSpeed250() {
        this.switchUpdateInterval(250.0D, this.speed250Button);
    }

    @FXML
    private void handleSpeed500() {
        this.switchUpdateInterval(500.0D, this.speed500Button);
    }
}
