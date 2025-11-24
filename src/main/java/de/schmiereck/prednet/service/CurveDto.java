package de.schmiereck.prednet.service;

public record CurveDto(long[] inputHistorieCurveArr, long[] outputHistorieCurveArr,
                       int netInputCurveLength, int netOutputCurveLength, long[] inputCurveArr,
                       long[] expectedOutputHistorieArr, long[] expectedOutputArr, long[] outputCurveArr) {
}
