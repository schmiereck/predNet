package de.schmiereck.prednet.service.normNet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NormNetService_WHEN_calcvalue_is_called_Test {


    @Test
    void GIVEN_2_layer_net_THEN_output_calculated() {
        final int[] layerNeuronCounts = new int[]{2, 2};
        final NormNet net = NormNetService.initNet(layerNeuronCounts);

        net.neuronList.get(3).parentSynapseList.get(0).weight = 100_000; // 0.1 in NormNeuron representation
        net.neuronList.get(3).parentSynapseList.get(1).weight = 200_000; // 0.2 in NormNeuron representation
        net.neuronList.get(3).parentSynapseList.get(2).weight = 0; // Bias.

        net.neuronList.get(2).parentSynapseList.get(0).weight = 200_000; // 0.2 in NormNeuron representation
        net.neuronList.get(2).parentSynapseList.get(1).weight = 400_000; // 0.4 in NormNeuron representation
        net.neuronList.get(2).parentSynapseList.get(2).weight = 100_000; // Bias.

        final int[] inputArr = new int[]{ 500, 1_000 };

        NormNetService.calcValue(net, inputArr);

        final int[] expectedOutputArr = new int[]{ 500, 250 };

        Assertions.assertEquals(expectedOutputArr[0], net.outputNeuronList.get(0).value);
        Assertions.assertEquals(expectedOutputArr[1], net.outputNeuronList.get(1).value);
    }
}
