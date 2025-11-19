package de.schmiereck.prednet.service;

import java.util.Arrays;

public class PredNetService {
    private final int[] precalcCurveArr = new int[]
            {
                    0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
                    90, 80, 70, 60, 50, 40, 30, 20, 10,
            };
    private volatile int xPosCurve; // volatile für Sichtbarkeit zwischen Threads
    private final int curveLength;
    private volatile int[] inputCurveArr; // volatile Referenz, wird in calc() neu erzeugt
    private volatile int output; // volatile Referenz, wird in calc() neu erzeugt
    private volatile int[] outputHistorieCurveArr; // volatile Referenz, wird in calc() neu erzeugt

    public PredNetService() {
        this.xPosCurve = 0;
        this.curveLength = 10;
        this.inputCurveArr = new int[this.curveLength];
        this.output = 0;
        this.outputHistorieCurveArr = new int[this.curveLength];
    }

    public CurveDto retrieveCurve() {
        // neue Kopie für den DTO (Isolation vom Hintergrund-Array)
        final int[] inputArr = Arrays.copyOf(this.inputCurveArr, this.curveLength);
        final int[] outputArr = Arrays.copyOf(this.outputHistorieCurveArr, this.curveLength);

        return new CurveDto(inputArr, outputArr, this.output);
    }

    public void calc() {
        this.calcInput();
        this.calcOutput();
        this.calcOutputHistorie();
    }

    private void calcInput() {
        // Neues Array aufbauen (Copy-on-Write) statt In-Place Mutation
        final int[] newArr = new int[this.curveLength];
        for (int posX = 0; posX < this.curveLength; posX++) {
            newArr[posX] = this.precalcCurveArr[(this.xPosCurve + posX) % this.precalcCurveArr.length];
        }
        // Nächste Position
        final int nextXPos = (this.xPosCurve + 1) % this.precalcCurveArr.length;
        // Veröffentlichung: zuerst das neue Array, dann den Index (oder umgekehrt; hier egal, beide volatile)
        this.inputCurveArr = newArr;
        this.xPosCurve = nextXPos;
    }

    private void calcOutput() {
        this.output = this.precalcCurveArr[(this.xPosCurve + this.curveLength - 1) % this.precalcCurveArr.length] / 2; // Beispielhafte Ausgabe: halber Wert
    }

    private void calcOutputHistorie() {
        final int[] newOutputArr = new int[this.curveLength];
        for (int posX = 0; posX < this.curveLength - 1; posX++) {
            newOutputArr[posX] = this.outputHistorieCurveArr[posX + 1];
        }
        newOutputArr[newOutputArr.length - 1] = this.output;
        this.outputHistorieCurveArr = newOutputArr;
    }

}
