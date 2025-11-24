package de.schmiereck.prednet.service;

public record CurveDto(long[] inputArr, long[] outputHistorieArr, long[] outputArr,
                       int netInputCurveLength, int netOutputCurveLength) {
}
