package de.schmiereck.prednet.service;

import de.schmiereck.prednet.service.baseNet.BaseNetService.CurvePoint;

public record CurveDto(CurvePoint[] inputHistorieCurveArr, CurvePoint[] outputHistorieCurveArr,
                       int netInputCurveLength, int netOutputCurveLength, CurvePoint[] inputCurveArr,
                       long[] expectedOutputHistorieArr, long[] expectedOutputArr, CurvePoint[] outputCurveArr, long mse) {
}
