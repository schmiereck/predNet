package de.schmiereck.prednet.service.normNet;

import org.junit.jupiter.api.Test;

public class NormNetService_WHEN_calcTrain_is_called_Test {

    @Test
    void GIVEN_3_layer_THEN_net_trained_for_one_input() {
        final int[] layerNeuronCounts = new int[]{2, 2, 2};
        final NormNet net = NormNetService.initNet(layerNeuronCounts);

        final long[] inputArr = new long[]{ 500, 1_000 };

        final long l2n0i = 5_000;
        final long l2n1i = 100;
        final long[] targetOutputArr = new long[]{ l2n0i, l2n1i };

        for (int iterationPos = 0; iterationPos < 14; iterationPos++) {
            NormNetService.calcValue(net, inputArr);
            final long mse = NormNetService.calcError(net, targetOutputArr);
            System.out.printf("mse: %d%n", mse);
            NormNetService.calcTrain(net, 100_000);
        }
    }
}
