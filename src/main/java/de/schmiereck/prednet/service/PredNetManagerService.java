package de.schmiereck.prednet.service;

import de.schmiereck.prednet.service.normNet.NormNetService;

import java.util.Arrays;

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

    private volatile long[] inputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt
    private int netInputCurveLength;
    private volatile long[] inputCurveArr; // volatile Referenz, wird in calc() neu erzeugt
    private volatile long[] outputCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int outputHistorieCurveLength;
    private volatile long[] outputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int netOutputCurveLength;
    private volatile long[] expectedOutputArr; // volatile Referenz, wird in calc() neu erzeugt
    private volatile long[] expectedOutputHistorieArr; // volatile Referenz, wird in calc() neu erzeugt

    private volatile int xPosCurve; // volatile für Sichtbarkeit zwischen Threads

    private long mse;

    private long iterationPos;
    private boolean useOutputAsInput;

    public PredNetManagerService() {
        this.predNetService = new PredNetService();
    }

    public void initNet(final CurveGeneratorService.CurveType curveType,
                        final int netInputCurveLength, final int netOutputCurveLength,
                        final int hiddenLayerCount,
                        final boolean useOutputAsInput) {
        final boolean useLoopbackMemory = false;
        final NormNetService.LoopbackType loopbackType = NormNetService.LoopbackType.None;

        this.initNet(curveType, netInputCurveLength, netOutputCurveLength,
                hiddenLayerCount, useOutputAsInput, loopbackType);
    }

    public void initNet(final CurveGeneratorService.CurveType curveType,
                        final int netInputCurveLength, final int netOutputCurveLength,
                        final int hiddenLayerCount,
                        final boolean useOutputAsInput,
                        final NormNetService.LoopbackType loopbackType) {
        this.curveType = curveType;

        this.curveLength = 45;

        this.netInputCurveLength = netInputCurveLength;
        this.inputCurveArr = null;
        this.outputCurveArr = new long[netOutputCurveLength];
        this.inputHistorieCurveArr = null;

        this.netOutputCurveLength = netOutputCurveLength;
        this.expectedOutputArr = null;
        this.expectedOutputHistorieArr = null;

        this.useOutputAsInput = useOutputAsInput;

        this.xPosCurve = 0;

        if (this.useOutputAsInput) {
            this.predNetService.initNet(this.netInputCurveLength + this.netOutputCurveLength,
                    this.netOutputCurveLength, hiddenLayerCount, loopbackType);
        } else {
            this.predNetService.initNet(this.netInputCurveLength,
                    this.netOutputCurveLength, hiddenLayerCount);
        }

        this.outputHistorieCurveLength = this.curveLength - this.netOutputCurveLength;
        this.outputHistorieCurveArr = new long[this.outputHistorieCurveLength +  this.netOutputCurveLength];

        this.iterationPos = 0;
    }

    public void runCalc() {
        this.inputHistorieCurveArr = calcInputCurve(this.curveType,
                this.xPosCurve,
                this.outputHistorieCurveLength);
        if (this.useOutputAsInput) {
            this.inputCurveArr = calcInputCurve(this.curveType,
                    this.xPosCurve + (this.curveLength - (this.netOutputCurveLength + this.netInputCurveLength)),
                    this.netInputCurveLength, this.netOutputCurveLength, this.outputCurveArr);
        } else {
            this.inputCurveArr = calcInputCurve(this.curveType,
                    this.xPosCurve + (this.curveLength - (this.netOutputCurveLength + this.netInputCurveLength)),
                    this.netInputCurveLength);
        }
        this.expectedOutputHistorieArr = calcExpectedOutputArr(this.curveType,
                this.xPosCurve,
                 this.curveLength - this.netOutputCurveLength);
        this.expectedOutputArr = calcExpectedOutputArr(this.curveType,
                this.xPosCurve + (this.curveLength - this.netOutputCurveLength),
                 this.netOutputCurveLength);

        final OutputDto outputDto = this.predNetService.runCalcOutput(this.inputCurveArr, expectedOutputArr);
        this.outputCurveArr = outputDto.outputArr();
        this.outputHistorieCurveArr = calcOutputHistorieCurve(this.outputHistorieCurveLength, this.netOutputCurveLength,
                this.outputHistorieCurveArr, this.outputCurveArr);

        this.mse = outputDto.mse();

        if (this.iterationPos % 13 == 0) {
            System.out.printf("Iter: %6d, Exp: %s, Out: %s, MSE: %13d%n", this.iterationPos,
                    printArray(expectedOutputArr),
                    printArray(outputCurveArr),
                    outputDto.mse());
        }

        // Nächste Position
        this.xPosCurve = (this.xPosCurve + 1);
        this.iterationPos++;
    }

    private static long[] calcInputCurve(final CurveGeneratorService.CurveType curveType, final int xPosCurve,
                                         final int curveLength) {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final long[] newInputCurveArr = new long[curveLength];
        for (int posX = 0; posX < curveLength; posX++) {
            newInputCurveArr[posX] = CurveGeneratorService.retrieveCurveValue(curveType,xPosCurve + posX);
        }
        return newInputCurveArr;
    }

    private static long[] calcInputCurve(final CurveGeneratorService.CurveType curveType, final int xPosCurve,
                                         final int inputCurveLength, final int outputCurveLength,
                                         final long[] outputCurveArr) {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final long[] newInputCurveArr = new long[inputCurveLength + outputCurveLength];
        for (int posX = 0; posX < inputCurveLength; posX++) {
            newInputCurveArr[posX] = CurveGeneratorService.retrieveCurveValue(curveType,xPosCurve + posX);
        }
        for (int posX = 0; posX < outputCurveLength; posX++) {
            newInputCurveArr[inputCurveLength + posX] = outputCurveArr[posX];
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

    private static long[] calcOutputHistorieCurve(final int historieCurveLength, final int netOutputCurveLength,
                                           final long[] oldOutputHistorieCurveArr, final long[] newOutputArr) {
        final long[] newOutputHistorieCurveArr = new long[historieCurveLength + netOutputCurveLength];
        for (int posX = 0; posX < (historieCurveLength); posX++) {
            newOutputHistorieCurveArr[posX] = oldOutputHistorieCurveArr[posX + 1];
        }

        for (int posX = 0; posX < netOutputCurveLength; posX++) {
            newOutputHistorieCurveArr[(historieCurveLength) + posX] = newOutputArr[posX];
        }
        return newOutputHistorieCurveArr;
    }

    public CurveDto retrieveCurve() {
        // neue Kopie für den DTO (Isolation vom Hintergrund-Array)
        final long[] inputHistorieCurveArr = Arrays.copyOf(this.inputHistorieCurveArr, this.outputHistorieCurveLength);
        final long[] outputHistorieCurveArr = Arrays.copyOf(this.outputHistorieCurveArr, this.outputHistorieCurveArr.length);

        return new CurveDto(inputHistorieCurveArr, outputHistorieCurveArr, this.netInputCurveLength, this.netOutputCurveLength, this.inputCurveArr,
                this.expectedOutputHistorieArr, this.expectedOutputArr, this.outputCurveArr, this.mse);
    }

    private static String printArray(final long[] arr) {
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
}
