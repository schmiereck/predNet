package de.schmiereck.prednet.service.normNet;

import de.schmiereck.prednet.service.baseNet.BaseNetService;

public class CurvePointTestUtils {

    public static BaseNetService.CurvePoint[] createCurvePointArrByValueArr(long[] inputArr) {
        final BaseNetService.CurvePoint[] inputPointArr = new BaseNetService.CurvePoint[inputArr.length];
        for (int inputPos = 0; inputPos < inputArr.length; inputPos++) {
            inputPointArr[inputPos] = new BaseNetService.CurvePoint(inputPos, inputArr[inputPos]);
        }
        return inputPointArr;
    }
}
