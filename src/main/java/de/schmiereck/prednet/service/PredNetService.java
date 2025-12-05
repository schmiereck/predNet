package de.schmiereck.prednet.service;

import de.schmiereck.prednet.service.baseNet.BaseNetService;
import de.schmiereck.prednet.service.integrationNet.IntegrationNetService;
import de.schmiereck.prednet.service.normNet.NormNet;
import de.schmiereck.prednet.service.normNet.NormNetService;
import de.schmiereck.prednet.service.normNet.NormNeuron;

import static de.schmiereck.prednet.service.normNet.NormNetUtils.calcValuePerc;

public class PredNetService {
    private NormNet net;
    private BaseNetService netService;

    public PredNetService() {
        this.netService = new NormNetService();
        //this.netService = new IntegrationNetService();
    }

    public void initNet(final int inputCurveLength, final int outputCurveLength, final int hiddenLayerCount) {
        final BaseNetService.LoopbackType loopbackType = BaseNetService.LoopbackType.None;
        this.initNet(inputCurveLength, outputCurveLength, hiddenLayerCount, loopbackType);
    }

    public void initNet(final int inputCurveLength, final int outputCurveLength, final int hiddenLayerCount,
                        final BaseNetService.LoopbackType loopbackType) {
        //final int[] layerNeuronCounts = new int[] {
        //        inputCurveLength,
        //        inputCurveLength * 2,
        //        inputCurveLength * 2,
        //        inputCurveLength * 2,
        //        outputCurveLength
        //};
        final int[] layerNeuronCounts = new int[1 + hiddenLayerCount + 1];
        layerNeuronCounts[0] = inputCurveLength;
        for (int hiddenLayerPos = 0; hiddenLayerPos < hiddenLayerCount; hiddenLayerPos++) {
            layerNeuronCounts[1 + hiddenLayerPos] = (inputCurveLength * 2);
        }
        layerNeuronCounts[1 + hiddenLayerCount] = outputCurveLength;

        this.net = this.netService.initNet(layerNeuronCounts, loopbackType);
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

        this.netService.calcValue(net, scaledInputCurveArr);

        //this.output = this.precalcCurveArr[(this.xPosCurve + this.curveLength - 1) % this.precalcCurveArr.length] / 2; // Beispielhafte Ausgabe: halber Wert
        //final long output = net.outputNeuronList.get(0).value * 100L / NormNeuron.MaxValue;
        final long[] outputArr = new long[expectedOutputArr.length];
        for (int outputPos = 0; outputPos < expectedOutputArr.length; outputPos++) {
            outputArr[outputPos] = net.outputNeuronList.get(outputPos).value * 100L / NormNeuron.MaxValue;
        }
        final long mse = this.netService.calcError(net, scaledExpectedOutputArr);
        this.netService.calcTrain(net, calcValuePerc(5));

        //final long scaledMse = mse;// * 100_000L / (NormNeuron.MaxValue);
        final long scaledMse = mse * 10_000L / (NormNeuron.MaxValue);

        return new OutputDto(outputArr, scaledMse);
    }

}
