package de.schmiereck.prednet.service;

import java.util.Arrays;

public class PredNetManagerService {
    private final PredNetService predNetService;

    private volatile int xPosCurve; // volatile für Sichtbarkeit zwischen Threads

    private CurveGeneratorService.CurveType curveType;
    private volatile long[] inputCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int curveLength;
    private volatile long[] outputArr; // volatile Referenz, wird in calc() neu erzeugt
    private volatile long[] outputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int netInputCurveLength;
    private int netOutputCurveLength;

    private long iterationPos;

    public PredNetManagerService() {
        this.predNetService = new PredNetService();
    }

    public void initNet(final CurveGeneratorService.CurveType curveType) {
        this.curveType = curveType;

        this.xPosCurve = 0;
        this.netInputCurveLength = 8;//this.curveLength / 4;
        this.netOutputCurveLength = 6;

        this.predNetService.initNet(this.netInputCurveLength, this.netOutputCurveLength);
        this.inputCurveArr = new long[this.curveLength];

        this.curveLength = 45;
        this.outputArr = null;
        this.outputHistorieCurveArr = new long[this.curveLength];

        this.iterationPos = 0;
    }

    public void runCalc() {
        this.calcInputCurve();

        final long[] expectedOutputArr = new long[this.netOutputCurveLength];
        for (int outputPos = 0; outputPos < this.netOutputCurveLength; outputPos++) {
            final long expectedOutput = CurveGeneratorService.retrieveCurveValue(this.curveType,
                    this.xPosCurve + (this.curveLength) + (outputPos));
            expectedOutputArr[outputPos] = expectedOutput;
        }

        // Nächste Position
        //final int nextXPos = (this.xPosCurve + 1) % CurveGeneratorService.retrieveCurveLength(this.curveType);
        final int nextXPos = (this.xPosCurve + 1);
        this.xPosCurve = nextXPos;

        final OutputDto outputDto = this.predNetService.runCalcOutput(this.inputCurveArr, expectedOutputArr);
        this.outputArr = outputDto.outputArr();
        this.calcOutputHistorieCurve(this.outputArr);

        if (this.iterationPos % 13 == 0) {
            System.out.printf("Iter: %6d, Exp: %s, Out: %s, MSE: %13d%n", this.iterationPos, expectedOutputArr, this.outputArr, outputDto.mse());
        }
        this.iterationPos++;
    }

    private void calcInputCurve() {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final long[] newInputCurveArr = new long[this.curveLength];
        for (int posX = 0; posX < this.curveLength; posX++) {
            newInputCurveArr[posX] = CurveGeneratorService.retrieveCurveValue(this.curveType,this.xPosCurve + posX);
        }

        // Veröffentlichung: zuerst das neue Array, dann den Index (oder umgekehrt; hier egal, beide volatile)
        this.inputCurveArr = newInputCurveArr;
    }

    private void calcOutputHistorieCurve(final long[] outputArr) {
        final long[] newOutputArr = new long[this.curveLength + this.netOutputCurveLength];
        for (int posX = 0; posX < (this.outputHistorieCurveArr.length - 1); posX++) {
            newOutputArr[posX] = this.outputHistorieCurveArr[posX + 1];
        }

        for (int posX = 0; posX < this.netOutputCurveLength; posX++) {
            newOutputArr[(newOutputArr.length - 1) - ((this.netOutputCurveLength - 1) - posX)] = outputArr[posX];
        }
        this.outputHistorieCurveArr = newOutputArr;
    }

    public CurveDto retrieveCurve() {
        // neue Kopie für den DTO (Isolation vom Hintergrund-Array)
        final long[] inputArr = Arrays.copyOf(this.inputCurveArr, this.curveLength);
        final long[] outputArr = Arrays.copyOf(this.outputHistorieCurveArr, this.outputHistorieCurveArr.length);

        return new CurveDto(inputArr, outputArr, this.outputArr, this.netInputCurveLength, this.netOutputCurveLength);
        //return this.predNetService.retrieveCurve();
    }

    public int retrieveNetOutputCurveLength() {
        return this.netOutputCurveLength;
    }
}
