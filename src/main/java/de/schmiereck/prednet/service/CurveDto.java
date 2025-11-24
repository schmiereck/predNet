package de.schmiereck.prednet.service;

public record CurveDto(long[] inputCurveArr, long[] outputHistorieCurveArr,
                       int netInputCurveLength, int netOutputCurveLength) {
}
