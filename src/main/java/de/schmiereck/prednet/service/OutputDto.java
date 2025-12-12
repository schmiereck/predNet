package de.schmiereck.prednet.service;

import de.schmiereck.prednet.service.baseNet.BaseNetService.CurvePoint;

public record OutputDto(CurvePoint[] outputArr, long mse) {
}
