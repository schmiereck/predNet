package de.schmiereck.prednet.service;

public class CurveDto {
    private final long[] inputArr;
    private final long[] outputHistorieArr;
    private final long[] outputArr;

    public CurveDto(final long[] inputArr, final long[] outputHistorieArr, final long[] outputArr) {
        this.inputArr = inputArr;
        this.outputHistorieArr = outputHistorieArr;
        this.outputArr = outputArr;
    }

    public long[] getInputArr() {
        return this.inputArr;
    }

    public long[] getOutputHistorieArr() {
        return this.outputHistorieArr;
    }

    public long[] getOutputArr() {
        return this.outputArr;
    }
}
