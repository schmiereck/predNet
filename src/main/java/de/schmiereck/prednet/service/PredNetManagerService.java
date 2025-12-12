package de.schmiereck.prednet.service;

import de.schmiereck.prednet.service.baseNet.BaseNetService;
import de.schmiereck.prednet.service.baseNet.BaseNetService.CurvePoint;

import java.util.Arrays;
import java.util.Objects;

/**
 *    xPosCurve
 *    v
 * ---|---------------------------------------------------------------------|
 *    | curveLength                                                         |
 *    |---------------------------------------------|                       |
 *    | expectedOutputHistorieArr                   |                       |
 *    |                                             |-----------------------|
 *    |                                             | expectedOutputArr     |
 *    |                       |---------------------|                       |
 *    |                       | inputCurveArr       |                       |
 */
public class PredNetManagerService {
    private final PredNetService predNetService;

    private CurveGeneratorService.CurveType curveType;

    private int curveLength;

    private volatile CurvePoint[] inputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt
    private int netInputCurveLength;
    private volatile CurvePoint[] inputCurveArr; // volatile Referenz, wird in calc() neu erzeugt
    private volatile CurvePoint[] outputCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int outputHistorieCurveLength;
    private volatile CurvePoint[] outputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int netOutputCurveLength;
    private volatile long[] expectedOutputArr; // volatile Referenz, wird in calc() neu erzeugt
    private volatile long[] expectedOutputHistorieArr; // volatile Referenz, wird in calc() neu erzeugt

    private volatile int xPosCurve; // volatile für Sichtbarkeit zwischen Threads

    private long mse;

    private long iterationPos;
    private boolean useOutputAsInput;
    private boolean useLogarithmicTimeSteps;

    public PredNetManagerService() {
        this.predNetService = new PredNetService();
    }

    public void initNet(final CurveGeneratorService.CurveType curveType,
                        final int netInputCurveLength, final int netOutputCurveLength,
                        final int hiddenLayerCount, final int hiddenLayerNeuronCount,
                        final boolean useOutputAsInput) {
        final BaseNetService.LoopbackType loopbackType = BaseNetService.LoopbackType.None;
        final boolean useLogarithmicTimeSteps = false;

        this.initNet(curveType, netInputCurveLength, netOutputCurveLength,
                hiddenLayerCount, hiddenLayerNeuronCount, useOutputAsInput, loopbackType, useLogarithmicTimeSteps);
    }

    public void initNet(final CurveGeneratorService.CurveType curveType,
                        final int netInputCurveLength, final int netOutputCurveLength,
                        final int hiddenLayerCount, final int hiddenLayerNeuronCount,
                        final boolean useOutputAsInput,
                        final BaseNetService.LoopbackType loopbackType,
                        final boolean useLogarithmicTimeSteps) {
        this.curveType = curveType;

        this.curveLength = 45;

        this.netInputCurveLength = netInputCurveLength;
        this.inputCurveArr = null;
        this.outputCurveArr = new CurvePoint[netOutputCurveLength];
        this.inputHistorieCurveArr = null;

        this.netOutputCurveLength = netOutputCurveLength;
        this.expectedOutputArr = null;
        this.expectedOutputHistorieArr = null;

        this.useOutputAsInput = useOutputAsInput;
        this.useLogarithmicTimeSteps = useLogarithmicTimeSteps;

        this.xPosCurve = 0;

        if (this.useOutputAsInput) {
            this.predNetService.initNet(this.netInputCurveLength + this.netOutputCurveLength,
                    this.netOutputCurveLength, hiddenLayerCount, hiddenLayerNeuronCount, loopbackType);
        } else {
            this.predNetService.initNet(this.netInputCurveLength,
                    this.netOutputCurveLength, hiddenLayerCount, hiddenLayerNeuronCount);
        }

        this.outputHistorieCurveLength = this.curveLength - this.netOutputCurveLength;
        this.outputHistorieCurveArr = new CurvePoint[this.outputHistorieCurveLength +  this.netOutputCurveLength];

        this.iterationPos = 0;
    }

    public void runCalc() {
        this.inputHistorieCurveArr = calcInputCurve(this.curveType,
                this.xPosCurve,
                this.outputHistorieCurveLength, this.useLogarithmicTimeSteps);
        if (this.useOutputAsInput) {
            this.inputCurveArr = calcInputCurve(this.curveType,
                    this.xPosCurve + (this.curveLength - (this.netOutputCurveLength + this.netInputCurveLength)),
                    this.netInputCurveLength, this.netOutputCurveLength, this.outputCurveArr);
        } else {
            this.inputCurveArr = calcInputCurve(this.curveType,
                    this.xPosCurve + (this.curveLength - (this.netOutputCurveLength + this.netInputCurveLength)),
                    this.netInputCurveLength, this.useLogarithmicTimeSteps);
        }
        this.expectedOutputHistorieArr = calcExpectedOutputArr(this.curveType,
                this.xPosCurve,
                 this.curveLength - this.netOutputCurveLength);
        this.expectedOutputArr = calcExpectedOutputArr(this.curveType,
                this.xPosCurve + (this.curveLength - this.netOutputCurveLength),
                 this.netOutputCurveLength);

        final OutputDto outputDto = this.predNetService.runCalcOutput(this.xPosCurve, this.inputCurveArr, expectedOutputArr);
        this.outputCurveArr = outputDto.outputArr();
        this.outputHistorieCurveArr = calcOutputHistorieCurve(this.xPosCurve, this.outputHistorieCurveLength, this.netOutputCurveLength,
                this.outputHistorieCurveArr, this.outputCurveArr);

        this.mse = outputDto.mse();

        if (this.iterationPos % 13 == 0) {
            System.out.printf("Iter: %6d, Exp: %s, Out: %s, MSE: %13d%n", this.iterationPos,
                    printLongArray(expectedOutputArr),
                    printArray(outputCurveArr),
                    outputDto.mse());
        }

        // Nächste Position
        this.xPosCurve = (this.xPosCurve + 1);
        this.iterationPos++;
    }

    private static CurvePoint[] calcInputCurve(final CurveGeneratorService.CurveType curveType, final int xPosCurve,
                                         final int curveLength, final boolean useLogarithmicTimeSteps) {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final CurvePoint[] newInputCurveArr = new CurvePoint[curveLength];
        for (int posX = 0; posX < curveLength; posX++) {
            final long timePosX;
            if (useLogarithmicTimeSteps) {
                timePosX = Math.round(Math.log10(posX + 1) / Math.log10(curveLength) * (curveLength - 1));
            } else {
                timePosX = xPosCurve + posX;
            }
            newInputCurveArr[posX] = new CurvePoint(timePosX, CurveGeneratorService.retrieveCurveValue(curveType, timePosX));
        }
        return newInputCurveArr;
    }

    private static CurvePoint[] calcInputCurve(final CurveGeneratorService.CurveType curveType, final int xPosCurve,
                                         final int inputCurveLength, final int outputCurveLength,
                                         final CurvePoint[] outputCurveArr) {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final CurvePoint[] newInputCurveArr = new CurvePoint[inputCurveLength + outputCurveLength];
        for (int posX = 0; posX < inputCurveLength; posX++) {
            final long timePos = xPosCurve + posX;
            newInputCurveArr[posX] = new CurvePoint(timePos, CurveGeneratorService.retrieveCurveValue(curveType, timePos));
        }
        for (int posX = 0; posX < outputCurveLength; posX++) {
            final long timePos = xPosCurve + inputCurveLength + posX;
            newInputCurveArr[inputCurveLength + posX] = new CurvePoint(timePos, Objects.nonNull(outputCurveArr[posX]) ? outputCurveArr[posX].value() : 0L);
        }
        return newInputCurveArr;
    }

    private static long[] calcExpectedOutputArr(final CurveGeneratorService.CurveType curveType, final int xPosCurve,
                                                final int netOutputCurveLength) {
        final long[] expectedOutputArr = new long[netOutputCurveLength];
        for (int outputPos = 0; outputPos < netOutputCurveLength; outputPos++) {
            final long expectedOutput = CurveGeneratorService.retrieveCurveValue(curveType,
                    xPosCurve + (outputPos));
            expectedOutputArr[outputPos] = expectedOutput;
        }
        return expectedOutputArr;
    }

    private static CurvePoint[] calcOutputHistorieCurve(final int xPosCurve, final int historieCurveLength, final int netOutputCurveLength,
                                           final CurvePoint[] oldOutputHistorieCurveArr, final CurvePoint[] newOutputArr) {
        final CurvePoint[] newOutputHistorieCurveArr = new CurvePoint[historieCurveLength + netOutputCurveLength];
        for (int posX = 0; posX < (historieCurveLength); posX++) {
            final long timePos = xPosCurve + posX;
            final CurvePoint oldOutputHistorieCurveePoint = oldOutputHistorieCurveArr[posX + 1];
            newOutputHistorieCurveArr[posX] = new CurvePoint(timePos, Objects.nonNull(oldOutputHistorieCurveePoint) ? oldOutputHistorieCurveePoint.value() : 0L);
        }

        for (int posX = 0; posX < netOutputCurveLength; posX++) {
            final long timePos = xPosCurve + (historieCurveLength) + posX;
            newOutputHistorieCurveArr[(historieCurveLength) + posX] = new CurvePoint(timePos, newOutputArr[posX].value());
        }
        return newOutputHistorieCurveArr;
    }

    public CurveDto retrieveCurve() {
        // neue Kopie für den DTO (Isolation vom Hintergrund-Array)
        final CurvePoint[] inputHistorieCurveArr = Arrays.copyOf(this.inputHistorieCurveArr, this.outputHistorieCurveLength);
        final CurvePoint[] outputHistorieCurveArr = Arrays.copyOf(this.outputHistorieCurveArr, this.outputHistorieCurveArr.length);

        return new CurveDto(inputHistorieCurveArr, outputHistorieCurveArr, this.netInputCurveLength, this.netOutputCurveLength, this.inputCurveArr,
                this.expectedOutputHistorieArr, this.expectedOutputArr, this.outputCurveArr, this.mse);
    }

    private static String printLongArray(final long[] arr) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(String.format("%4d", arr[i]));
            if (i < (arr.length - 1)) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String printArray(final CurvePoint[] arr) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(String.format("%4d", arr[i].value()));
            if (i < (arr.length - 1)) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
