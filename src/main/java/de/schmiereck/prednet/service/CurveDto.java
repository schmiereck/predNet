package de.schmiereck.prednet.service;

public class CurveDto {
    private final long[] inputArr;
    private final long[] outputHistorieArr;
    private final long output;

    public CurveDto(final long[] inputArr, final long[] outputHistorieArr, final long output) {
        this.inputArr = inputArr;
        this.outputHistorieArr = outputHistorieArr;
        this.output = output;
    }

    public long[] getInputArr() {
        return this.inputArr;
    }

    public long[] getOutputHistorieArr() {
        return this.outputHistorieArr;
    }

    public long getOutput() {
        return this.output;
    }
}
