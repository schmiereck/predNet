package de.schmiereck.prednet.service.normNet;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static de.schmiereck.prednet.service.normNet.NormNetUtils.calcValuePerc;

public class NormNetService_WHEN_calcTrain_is_called_Test {

    @Test
    void GIVEN_3_layer_THEN_net_trained_for_one_input() {
        final int[] layerNeuronCounts = new int[]{ 2, 2, 2 };
        final NormNetService normNetService = new NormNetService();
        final NormNet net = normNetService.initNet(layerNeuronCounts);

        final long[] inputArr = new long[]{ calcValuePerc(10), calcValuePerc(80) };

        final long[] targetOutputArr = new long[]{ calcValuePerc(80), calcValuePerc(10) };

        for (int iterationPos = 0; iterationPos < 25; iterationPos++) {
            normNetService.calcValue(net, inputArr);
            final long mse = normNetService.calcError(net, targetOutputArr);
            System.out.printf("iter: %d: mse: %d%n", iterationPos, mse);
            normNetService.calcTrain(net, calcValuePerc(50));

            if (iterationPos % 100 == 0) {
                showResult(normNetService, net, inputArr, targetOutputArr);
            }
        }
        showResult(normNetService, net, inputArr, targetOutputArr);
    }

    private void showResult(final NormNetService normNetService, NormNet net, long[] inputArr, long[] targetOutputArr) {
        System.out.println("---- Ergebnis ----");
        normNetService.calcValue(net, inputArr);
        System.out.printf("in: [%6d, %6d] -> out: [%6d, %6d] (soll: [%6d, %6d])%n",
                inputArr[0], inputArr[1],
                net.outputNeuronList.get(0).value, net.outputNeuronList.get(1).value,
                targetOutputArr[0], targetOutputArr[1]);
    }

    @Test
    void GIVEN_x_layer_and_4_inputs_THEN_AND_net_trained() {
        final int[] layerNeuronCounts = new int[]{ 2, 4, 4, 2 };
        final NormNetService normNetService = new NormNetService();
        final NormNet net = normNetService.initNet(layerNeuronCounts);

        final long[][] inputArrArr = new long[][]
                {
                        { calcValuePerc(0), calcValuePerc(0) },
                        { calcValuePerc(0), calcValuePerc(100) },
                        { calcValuePerc(100), calcValuePerc(0) },
                        { calcValuePerc(100), calcValuePerc(100) }
                };

        final long[][] targetOutputArrArr = new long[][]
                {
                        { calcValuePerc(0), calcValuePerc(0) },
                        { calcValuePerc(0), calcValuePerc(0) },
                        { calcValuePerc(0), calcValuePerc(0) },
                        { calcValuePerc(100), calcValuePerc(100) }
                };

        final Random rnd = new Random(28);

        for (int iterationPos = 0; iterationPos < 500; iterationPos++) {
            for (int xdataPos = 0; xdataPos < inputArrArr.length; xdataPos++) {
                final int dataPos = rnd.nextInt(inputArrArr.length);
                final long[] inputArr = inputArrArr[dataPos];
                final long[] targetOutputArr = targetOutputArrArr[dataPos];

                normNetService.calcValue(net, inputArr);
                final long mse = normNetService.calcError(net, targetOutputArr);
                System.out.printf("iter: %4d: mse: %13d%n", iterationPos, mse);
                normNetService.calcTrain(net, calcValuePerc(25));
            }
            if (iterationPos % 1000 == 0) {
                showResult(normNetService, net, inputArrArr, targetOutputArrArr);
            }
        }
        showResult(normNetService, net, inputArrArr, targetOutputArrArr);
    }

    @Test
    void GIVEN_x_layer_and_4_inputs_THEN_net_trained() {
        final int[] layerNeuronCounts = new int[]{ 2, 4, 8, 4, 2 };
        final NormNetService normNetService = new NormNetService();
        final NormNet net = normNetService.initNet(layerNeuronCounts);

        final long[][] inputArrArr = new long[][]
                {
                        { calcValuePerc(0), calcValuePerc(0) },
                        { calcValuePerc(0), calcValuePerc(60) },
                        { calcValuePerc(60), calcValuePerc(0) },
                        { calcValuePerc(60), calcValuePerc(60) }
                };

        final long[][] targetOutputArrArr = new long[][]
                {
                        { calcValuePerc(60), calcValuePerc(0) },
                        { calcValuePerc(0), calcValuePerc(30) },
                        { calcValuePerc(0), calcValuePerc(30) },
                        { calcValuePerc(60), calcValuePerc(60) }
                };

        for (int iterationPos = 0; iterationPos < 150; iterationPos++) {
            for (int dataPos = 0; dataPos < inputArrArr.length; dataPos++) {
                final long[] inputArr = inputArrArr[dataPos];
                final long[] targetOutputArr = targetOutputArrArr[dataPos];

                normNetService.calcValue(net, inputArr);
                final long mse = normNetService.calcError(net, targetOutputArr);
                System.out.printf("iter: %d: mse: %d%n", iterationPos, mse);
                normNetService.calcTrain(net, calcValuePerc(30));
            }
            if (iterationPos % 1000 == 0) {
                showResult(normNetService, net, inputArrArr, targetOutputArrArr);
            }
        }
        showResult(normNetService, net, inputArrArr, targetOutputArrArr);
    }

    private void showResult(final NormNetService normNetService, NormNet net, long[][] inputArrArr, long[][] targetOutputArrArr) {
        System.out.println("---- Ergebnis ----");
        for (int dataPos = 0; dataPos < inputArrArr.length; dataPos++) {
            final long[] inputArr = inputArrArr[dataPos];
            final long[] targetOutputArr = targetOutputArrArr[dataPos];

            normNetService.calcValue(net, inputArr);
            System.out.printf("in: [%7d, %7d] -> out: [%7d, %7d] (soll: [%7d, %7d])%n",
                    inputArr[0], inputArr[1],
                    net.outputNeuronList.get(0).value, net.outputNeuronList.get(1).value,
                    targetOutputArr[0], targetOutputArr[1]);
        }
    }
}
