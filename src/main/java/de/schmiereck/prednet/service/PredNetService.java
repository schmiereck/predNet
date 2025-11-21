package de.schmiereck.prednet.service;

import de.schmiereck.prednet.service.normNet.NormNet;
import de.schmiereck.prednet.service.normNet.NormNetService;
import de.schmiereck.prednet.service.normNet.NormNeuron;

import java.util.Arrays;

import static de.schmiereck.prednet.service.normNet.NormNetUtils.calcValuePerc;

public class PredNetService {
    private NormNet net;

    public PredNetService() {
    }

    public void initNet(final int inputCurveLength, final int outputCurveLength) {
        final int[] layerNeuronCounts = new int[] {
                inputCurveLength,
                inputCurveLength * 2,
                inputCurveLength * 2,
                inputCurveLength,
                outputCurveLength
        };
        this.net = NormNetService.initNet(layerNeuronCounts);
    }

    public OutputDto runCalcOutput(final long[] inputCurveArr, final long[] expectedOutputArr) {
        final long[] scaledInputCurveArr = new long[inputCurveArr.length];
        for (int posX = 0; posX < scaledInputCurveArr.length; posX++) {
            scaledInputCurveArr[posX] = inputCurveArr[posX] * NormNeuron.MaxValue / 100L;
        }

        final long[] scaledExpectedOutputArr = new long[expectedOutputArr.length];
        for (int expectedOutputPos = 0; expectedOutputPos < expectedOutputArr.length; expectedOutputPos++) {
            scaledExpectedOutputArr[expectedOutputPos] = expectedOutputArr[expectedOutputPos] * NormNeuron.MaxValue / 100L;
        }

        NormNetService.calcValue(net, scaledInputCurveArr);

        //this.output = this.precalcCurveArr[(this.xPosCurve + this.curveLength - 1) % this.precalcCurveArr.length] / 2; // Beispielhafte Ausgabe: halber Wert
        //final long output = net.outputNeuronList.get(0).value * 100L / NormNeuron.MaxValue;
        final long[] outputArr = new long[expectedOutputArr.length];
        for (int outputPos = 0; outputPos < expectedOutputArr.length; outputPos++) {
            outputArr[outputPos] = net.outputNeuronList.get(outputPos).value * 100L / NormNeuron.MaxValue;
        }
        final long mse = NormNetService.calcError(net, scaledExpectedOutputArr);
        NormNetService.calcTrain(net, calcValuePerc(5));

        //final long scaledMse = mse;// * 100_000L / (NormNeuron.MaxValue);
        final long scaledMse = mse * 10_000L / (NormNeuron.MaxValue);

        return new OutputDto(outputArr, scaledMse);
    }

}
