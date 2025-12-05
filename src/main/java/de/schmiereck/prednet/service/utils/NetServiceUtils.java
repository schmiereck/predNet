package de.schmiereck.prednet.service.utils;

import de.schmiereck.prednet.service.baseNet.BaseNetService;
import de.schmiereck.prednet.service.normNet.NormNet;
import de.schmiereck.prednet.service.normNet.NormNeuron;
import de.schmiereck.prednet.service.normNet.NormSynapse;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public abstract class NetServiceUtils {
    private NetServiceUtils() {
    }

    @NotNull
    public static NormNet getNormNet(final Random rnd,
                                     final int[] layerNeuronCounts, final BaseNetService.LoopbackType loopbackType) {
        final NormNet net = new NormNet();

        // Create neurons:
        NormNeuron[] parentLayerNeuronArr = null;
        for (int layerPos = 0; layerPos < layerNeuronCounts.length; layerPos++) {
            final int neuronCounts = layerNeuronCounts[layerPos];
            final NormNeuron[] layerNeuronArr = new NormNeuron[neuronCounts];
            for (int neuronPos = 0; neuronPos < neuronCounts; neuronPos++) {
                NormNeuron.NeuronType neuronType =
                        (layerPos == 0) ? NormNeuron.NeuronType.Input :
                                (layerPos == layerNeuronCounts.length - 1) ? NormNeuron.NeuronType.Output :
                                        NormNeuron.NeuronType.Hidden;
                final NormNeuron neuron = new NormNeuron(neuronType, NormNeuron.NullValue);

                // Create synapses from parent layer to this neuron.
                if (layerPos > 0) {
                    final int inputCount = parentLayerNeuronArr.length + 1; // +1 for bias
                    for (final NormNeuron parentLayerNeuron : parentLayerNeuronArr) {
                        final NormSynapse synapse = new NormSynapse(parentLayerNeuron, calcInitWeight(rnd, inputCount), neuron);
                        neuron.parentSynapseList.add(synapse);
                        parentLayerNeuron.childSynapseList.add(synapse);
                    }
                    if ((loopbackType != BaseNetService.LoopbackType.None) && (neuronType == NormNeuron.NeuronType.Hidden)) {
                        // Add loopback memory synapse.
                        switch (loopbackType) {
                            case Neuron -> {
                                final NormSynapse loopbackSynapse = new NormSynapse(neuron, calcInitWeight(rnd, inputCount), neuron);
                                loopbackSynapse.loopback = true;
                                neuron.parentSynapseList.add(loopbackSynapse);
                                neuron.childSynapseList.add(loopbackSynapse);
                            }
                            case ParentNeuron -> {
                                final NormNeuron parentLayerNeuron = parentLayerNeuronArr[neuronPos];
                                final NormSynapse loopbackSynapse = new NormSynapse(parentLayerNeuron, calcInitWeight(rnd, inputCount), neuron);
                                loopbackSynapse.loopback = true;
                                parentLayerNeuron.parentSynapseList.add(loopbackSynapse);
                                neuron.childSynapseList.add(loopbackSynapse);
                            }
                        }
                        //final NormSynapse loopbackSynapse = new NormSynapse(neuron, calcInitWeight(inputCount), neuron);
                        final NormNeuron parentLayerNeuron = parentLayerNeuronArr[neuronPos];
                        final NormSynapse loopbackSynapse = new NormSynapse(parentLayerNeuron, calcInitWeight(rnd, inputCount), neuron);
                        loopbackSynapse.loopback = true;
                        //neuron.parentSynapseList.add(loopbackSynapse);
                        parentLayerNeuron.parentSynapseList.add(loopbackSynapse);
                        neuron.childSynapseList.add(loopbackSynapse);
                    }
                    // Add bias synapse.
                    final NormSynapse synapse = new NormSynapse(net.biasNeuron, calcInitWeight(rnd, inputCount), neuron);
                    neuron.parentSynapseList.add(synapse);
                }
                layerNeuronArr[neuronPos] = neuron;
                net.neuronList.add(neuron);
            }
            if (layerPos == 0) {
                net.inputNeuronList.addAll(java.util.Arrays.asList(layerNeuronArr));
            }
            if (layerPos == layerNeuronCounts.length - 1) {
                net.outputNeuronList.addAll(java.util.Arrays.asList(layerNeuronArr));
            }
            parentLayerNeuronArr = layerNeuronArr;
        }
        return net;
    }

    /**
     * Xavier/Glorot Initialisierung für normalized values.
     * Gewichte im Bereich [-MaxValue/sqrt(n), +MaxValue/sqrt(n)]
     * wobei n = Anzahl Input-Neuronen
     */
    private static long calcInitWeight(final Random rnd) {
        // Zufallswert zwischen -0.1 und +0.1 (10% von MaxValue)
        final double randomValue = (rnd.nextDouble() * 2.0 - 1.0) * 0.1;
        return (long) (randomValue * NormNeuron.MaxValue);
    }

    /**
     * Alternative: He-Initialisierung (besser für ReLU-artige Funktionen)
     */
    private static long calcInitWeight(final Random rnd, final int inputCount) {
        // He-Initialisierung: sqrt(2/n)
        final double scale = Math.sqrt(2.0 / inputCount);
        final double randomValue = rnd.nextGaussian() * scale;
        return (long) Math.max(NormNeuron.MinValue,
                Math.min(NormNeuron.MaxValue,
                        randomValue * NormNeuron.MaxValue));
    }
}
