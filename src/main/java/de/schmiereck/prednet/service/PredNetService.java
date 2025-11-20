package de.schmiereck.prednet.service;

import de.schmiereck.prednet.service.normNet.NormNet;
import de.schmiereck.prednet.service.normNet.NormNetService;
import de.schmiereck.prednet.service.normNet.NormNeuron;

import java.util.Arrays;

import static de.schmiereck.prednet.service.normNet.NormNetUtils.calcValuePerc;

public class PredNetService {
    private final int[] precalcCurveArr;
    private volatile int xPosCurve; // volatile für Sichtbarkeit zwischen Threads
    private final int curveLength;
    private volatile long[] inputCurveArr; // volatile Referenz, wird in calc() neu erzeugt
    private volatile long output; // volatile Referenz, wird in calc() neu erzeugt
    private volatile long[] outputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    private int netCurveLength;
    private final NormNet net;

    public PredNetService(final int curveType) {
        this.precalcCurveArr =
            switch (curveType) {
                case 0 ->
                    new int[]
                            {
                                    0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
                                    90, 80, 70, 60, 50, 40, 30, 20, 10,
                                    0, -10, -20, -30, -40, -50, -60, -70, -80, -90, -100,
                                    -90, -80, -70, -60, -50, -40, -30, -20, -10
                            };
                default -> throw  new IllegalArgumentException("Invalid curve type: " + curveType);
            };


        this.xPosCurve = 0;
        this.curveLength = 45;
        this.inputCurveArr = new long[this.curveLength];
        this.output = 0;
        this.outputHistorieCurveArr = new long[this.curveLength];

        this.netCurveLength = 7;//this.curveLength / 4;
        final int[] layerNeuronCounts = new int[] {
                this.netCurveLength,
                this.netCurveLength * 2,
                this.netCurveLength * 2,
                this.netCurveLength,
                1
        };
        this.net = NormNetService.initNet(layerNeuronCounts);
    }

    public CurveDto retrieveCurve() {
        // neue Kopie für den DTO (Isolation vom Hintergrund-Array)
        final long[] inputArr = Arrays.copyOf(this.inputCurveArr, this.curveLength);
        final long[] outputArr = Arrays.copyOf(this.outputHistorieCurveArr, this.curveLength);

        return new CurveDto(inputArr, outputArr, this.output);
    }

    public void calc() {
        this.calcInput();
        this.calcOutput();
        this.calcOutputHistorie();
    }

    private void calcInput() {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final long[] newArr = new long[this.curveLength];
        for (int posX = 0; posX < this.curveLength; posX++) {
            newArr[posX] = this.precalcCurveArr[(this.xPosCurve + posX) % this.precalcCurveArr.length];
        }
        // Nächste Position
        final int nextXPos = (this.xPosCurve + 1) % this.precalcCurveArr.length;
        // Veröffentlichung: zuerst das neue Array, dann den Index (oder umgekehrt; hier egal, beide volatile)
        this.inputCurveArr = newArr;
        this.xPosCurve = nextXPos;
    }
    private long iterationPos = 0;

    private void calcOutput() {
        final long[] expectedOutputArr = new long[1];
        final long expectedOutput = this.precalcCurveArr[(this.xPosCurve + this.curveLength - 1) % this.precalcCurveArr.length];
        expectedOutputArr[0] = expectedOutput * NormNeuron.MaxValue / 100L;

        final long[] inputArr = new long[this.netCurveLength];
        for (int posX = 0; posX < this.netCurveLength; posX++) {
            inputArr[posX] = this.inputCurveArr[posX] * NormNeuron.MaxValue / 100L;
        }
        NormNetService.calcValue(net, inputArr);

        //this.output = this.precalcCurveArr[(this.xPosCurve + this.curveLength - 1) % this.precalcCurveArr.length] / 2; // Beispielhafte Ausgabe: halber Wert
        this.output = net.outputNeuronList.get(0).value * 100L / NormNeuron.MaxValue;

        final long mse = NormNetService.calcError(net, expectedOutputArr);
        NormNetService.calcTrain(net, calcValuePerc(5));

        if (this.iterationPos % 13 == 0) {
            System.out.printf("Iter: %6d, Exp: %4d, Out: %4d, MSE: %13d%n", this.iterationPos, expectedOutput, this.output, mse);
        }
        this.iterationPos++;
    }

    private void calcOutputHistorie() {
        final long[] newOutputArr = new long[this.curveLength];
        for (int posX = 0; posX < this.curveLength - 1; posX++) {
            newOutputArr[posX] = this.outputHistorieCurveArr[posX + 1];
        }
        newOutputArr[newOutputArr.length - 1] = this.output;
        this.outputHistorieCurveArr = newOutputArr;
    }

}
