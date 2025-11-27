package de.schmiereck.prednet.service.normNet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NormNetService_WHEN_calcError_is_called_Test {

    @Test
    void GIVEN_3_layer_THEN_net_initialized() {
        final int[] layerNeuronCounts = new int[]{ 2, 2, 2 };
        final NormNetService normNetService = new NormNetService();
        final NormNet net = normNetService.initNet(layerNeuronCounts);

        for (final NormNeuron neuron : net.neuronList) {
            for (final NormSynapse synapse : neuron.parentSynapseList) {
                if (synapse.parentNeuron.neuronType == NormNeuron.NeuronType.Bias) {
                    synapse.weight = 0; // Bias.
                } else {
                    synapse.weight = 100_000; // 0.1 in NormNeuron representation
                }
            }
        }

        final long l2n0i = 500;
        final long l2n1i = 1_000;
        final long[] targetOutputArr = new long[]{ l2n0i, l2n1i };

        normNetService.calcError(net, targetOutputArr);

        // Assert Outputs:
        final long l2n0e = l2n0i - 0;
        final long l2n1e = l2n1i - 0;
        final long[] expectedOutputErrorArr = new long[]{ 500, 1_000 };

        for (int neuronPos = 0; neuronPos < net.outputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.outputNeuronList.get(neuronPos);
            final long expectedError = expectedOutputErrorArr[neuronPos];
            Assertions.assertEquals(expectedError, neuron.error, "Output-Neuron Pos: " + neuronPos);
        }

        // Assert Hidden-Errors:

        //final int l2n0e = ((500 * 100_000) / NormNeuron.MaxValue);
        //final int l2n1e = ((1_000 * 100_000) / NormNeuron.MaxValue);

        //final int l1n0e = ((l2n0e * 100_000) / NormNeuron.MaxValue) + ((l2n1e * 100_000) / NormNeuron.MaxValue);
        //final int l1n1e = ((l2n0e * 100_000) / NormNeuron.MaxValue) + ((l2n1e * 100_000) / NormNeuron.MaxValue);
        final long l1n0e = ((l2n0e * 100_000) / NormNeuron.MaxValue) + ((l2n1e * 100_000) / NormNeuron.MaxValue);
        final long l1n1e = ((l2n0e * 100_000) / NormNeuron.MaxValue) + ((l2n1e * 100_000) / NormNeuron.MaxValue);

        {
            final int neuronPos = 2;
            final NormNeuron neuron = net.neuronList.get(neuronPos);
            Assertions.assertEquals(l1n0e, neuron.error, "Input-Neuron Pos: " + neuronPos);
        }
        {
            final int neuronPos = 3;
            final NormNeuron neuron = net.neuronList.get(neuronPos);
            Assertions.assertEquals(l1n1e, neuron.error, "Input-Neuron Pos: " + neuronPos);
        }

        // Assert Input-Errors:

        final long l0n0e =
                ((l1n0e * 100_000) / NormNeuron.MaxValue) + ((l1n1e * 100_000) / NormNeuron.MaxValue);
        final long l0n1e =
                ((l1n0e * 100_000) / NormNeuron.MaxValue) + ((l1n1e * 100_000) / NormNeuron.MaxValue);

        //final int[] expectedInputErrorArr = new int[]{ 300, 300 };
        final long[] expectedInputErrorArr = new long[] { l0n0e, l0n1e };

        for (int neuronPos = 0; neuronPos < net.inputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.inputNeuronList.get(neuronPos);
            final long expectedError = expectedInputErrorArr[neuronPos];
            Assertions.assertEquals(expectedError, neuron.error, "Input-Neuron Pos: " + neuronPos);
        }
    }
}
