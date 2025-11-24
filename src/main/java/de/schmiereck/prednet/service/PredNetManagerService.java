package de.schmiereck.prednet.service;

import java.util.Arrays;

public class PredNetManagerService {
    private final PredNetService predNetService;

    private CurveGeneratorService.CurveType curveType;
    private volatile long[] inputCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int historieCurveLength;
    private volatile long[] outputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int netInputCurveLength;
    private int netOutputCurveLength;

    private volatile int xPosCurve; // volatile für Sichtbarkeit zwischen Threads

    private long iterationPos;

    public PredNetManagerService() {
        this.predNetService = new PredNetService();
    }

    public void initNet(final CurveGeneratorService.CurveType curveType,
                        final int netInputCurveLength, final int netOutputCurveLength) {
        this.curveType = curveType;

        this.netInputCurveLength = netInputCurveLength;
        this.netOutputCurveLength = netOutputCurveLength;

        this.xPosCurve = 0;

        this.predNetService.initNet(this.netInputCurveLength, this.netOutputCurveLength);

        this.inputCurveArr = null;
        this.historieCurveLength = 45;
        this.outputHistorieCurveArr = new long[this.historieCurveLength];

        this.iterationPos = 0;
    }

    public void runCalc() {
        this.inputCurveArr = calcInputCurve(this.curveType, this.xPosCurve, this.historieCurveLength);

        final long[] expectedOutputArr = new long[this.netOutputCurveLength];
        for (int outputPos = 0; outputPos < this.netOutputCurveLength; outputPos++) {
            final long expectedOutput = CurveGeneratorService.retrieveCurveValue(this.curveType,
                    this.xPosCurve + (this.historieCurveLength) + (outputPos));
            expectedOutputArr[outputPos] = expectedOutput;
        }

        // Nächste Position
        //final int nextXPos = (this.xPosCurve + 1) % CurveGeneratorService.retrieveCurveLength(this.curveType);
        final int nextXPos = (this.xPosCurve + 1);
        this.xPosCurve = nextXPos;

        final OutputDto outputDto = this.predNetService.runCalcOutput(this.inputCurveArr, expectedOutputArr);
        final long[] newOutputArr = outputDto.outputArr();
        this.outputHistorieCurveArr = calcOutputHistorieCurve(this.historieCurveLength, this.netOutputCurveLength,
                this.outputHistorieCurveArr, newOutputArr);

        if (this.iterationPos % 13 == 0) {
            System.out.printf("Iter: %6d, Exp: %s, Out: %s, MSE: %13d%n", this.iterationPos,
                    printArray(expectedOutputArr),
                    printArray(newOutputArr),
                    outputDto.mse());
        }
        this.iterationPos++;
    }

    private static String printArray(final long[] arr) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(String.format("%3d", arr[i]));
            if (i < (arr.length - 1)) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static long[] calcInputCurve(final CurveGeneratorService.CurveType curveType, final int xPosCurve, final int historieCurveLength) {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final long[] newInputCurveArr = new long[historieCurveLength];
        for (int posX = 0; posX < historieCurveLength; posX++) {
            newInputCurveArr[posX] = CurveGeneratorService.retrieveCurveValue(curveType,xPosCurve + posX);
        }
        return newInputCurveArr;
    }

    private static long[] calcOutputHistorieCurve(final int historieCurveLength, final int netOutputCurveLength,
                                           final long[] oldOutputHistorieCurveArr, final long[] newOutputArr) {
        final long[] newOutputHistorieCurveArr = new long[historieCurveLength + netOutputCurveLength];
        for (int posX = 0; posX < (oldOutputHistorieCurveArr.length - 1); posX++) {
            newOutputHistorieCurveArr[posX] = oldOutputHistorieCurveArr[posX + 1];
        }

        for (int posX = 0; posX < netOutputCurveLength; posX++) {
            newOutputHistorieCurveArr[(newOutputHistorieCurveArr.length - 1) - ((netOutputCurveLength - 1) - posX)] = newOutputArr[posX];
        }
        return newOutputHistorieCurveArr;
    }

    public CurveDto retrieveCurve() {
        // neue Kopie für den DTO (Isolation vom Hintergrund-Array)
        final long[] inputCurveArr = Arrays.copyOf(this.inputCurveArr, this.historieCurveLength);
        final long[] outputHistorieCurveArr = Arrays.copyOf(this.outputHistorieCurveArr, this.outputHistorieCurveArr.length);

        return new CurveDto(inputCurveArr, outputHistorieCurveArr, this.netInputCurveLength, this.netOutputCurveLength);
    }

    public int retrieveNetOutputCurveLength() {
        return this.netOutputCurveLength;
    }
}
