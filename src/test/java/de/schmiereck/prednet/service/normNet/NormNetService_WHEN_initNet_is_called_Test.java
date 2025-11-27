package de.schmiereck.prednet.service.normNet;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class NormNetService_WHEN_initNet_is_called_Test {

    @Test
    void GIVEN_3_layer_THEN_net_initialized() {
        final int[] layerNeuronCounts = new int[]{2, 3, 2};
        final int extraNeuronCount = 1; // Bias neuron
        final NormNetService normNetService = new NormNetService();
        final NormNet net = normNetService.initNet(layerNeuronCounts);

        // Check total neuron count
        final int expectedTotalNeurons = 2 + 3 + 2;
        assertEquals(expectedTotalNeurons, net.neuronList.size());

        // Check synapse connections
        int neuronIndex = 0;

        // First layer neurons (no synapses)
        for (int i = 0; i < layerNeuronCounts[0]; i++, neuronIndex++) {
            final NormNeuron neuron = net.neuronList.get(neuronIndex);
            assertEquals(0, neuron.parentSynapseList.size());
        }

        // Second layer neurons (should have synapses from first layer)
        for (int i = 0; i < layerNeuronCounts[1]; i++, neuronIndex++) {
            final NormNeuron neuron = net.neuronList.get(neuronIndex);
            assertEquals(layerNeuronCounts[0] + extraNeuronCount, neuron.parentSynapseList.size());
        }

        // Third layer neurons (should have synapses from second layer)
        for (int i = 0; i < layerNeuronCounts[2]; i++, neuronIndex++) {
            final NormNeuron neuron = net.neuronList.get(neuronIndex);
            assertEquals(layerNeuronCounts[1] + extraNeuronCount, neuron.parentSynapseList.size());
        }

        // Input neurons check
        assertEquals(layerNeuronCounts[0], net.inputNeuronList.size());

        // Output neurons check
        assertEquals(layerNeuronCounts[layerNeuronCounts.length - 1], net.outputNeuronList.size());
    }
}