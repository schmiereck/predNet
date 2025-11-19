package de.schmiereck.prednet.service;

public class CurveDto {
    private final int[] inputArr;
    private final int[] outputHistorieArr;
    private final int output;

    public CurveDto(final int[] inputArr, final int[] outputHistorieArr, final int output) {
        this.inputArr = inputArr;
        this.outputHistorieArr = outputHistorieArr;
        this.output = output;
    }

    public int[] getInputArr() {
        return this.inputArr;
    }

    public int[] getOutputHistorieArr() {
        return this.outputHistorieArr;
    }

    public int getOutput() {
        return this.output;
    }
}
