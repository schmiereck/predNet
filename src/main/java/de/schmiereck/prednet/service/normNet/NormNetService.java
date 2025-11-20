package de.schmiereck.prednet.service.normNet;

public class NormNetService {
    public static NormNet initNet(final int[] layerNeuronCounts) {
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
                    for (final NormNeuron parentLayerNeuron : parentLayerNeuronArr) {
                        final NormSynapse synapse = new NormSynapse(parentLayerNeuron, NormNeuron.NullValue, neuron);
                        neuron.parentSynapseList.add(synapse);
                        parentLayerNeuron.childSynapseList.add(synapse);
                    }
                    // Add bias synapse.
                    final NormSynapse synapse = new NormSynapse(net.biasNeuron, NormNeuron.NullValue, neuron);
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

    public static void calcError(final NormNet net, final long[] targetOutputArr) {
        for (final NormNeuron neuron : net.neuronList) {
            neuron.error = 0;
        }

        for (int neuronPos = 0; neuronPos < net.outputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.outputNeuronList.get(neuronPos);
            final long targetOutput = targetOutputArr[neuronPos];
            neuron.error = targetOutput - neuron.value;
        }
        for (final NormNeuron neuron : net.outputNeuronList) {
            calcError(neuron, neuron.error);
        }
    }

    private static void calcError(final NormNeuron neuron, final long error) {
        for (final NormSynapse synapse : neuron.parentSynapseList) {
            calcError(neuron, synapse, error);
        }
    }

    private static void calcError(final NormNeuron neuron, final NormSynapse synapse, final long error) {
        final NormNeuron parentNeuron = synapse.parentNeuron;
        final long propagatedError = (error * synapse.weight) / NormNeuron.MaxValue;
        parentNeuron.error += propagatedError;

        calcError(parentNeuron, error);
    }

    public static void calcValue(final NormNet net, final long[] inputArr) {
        for (int neuronPos = 0; neuronPos < net.inputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.inputNeuronList.get(neuronPos);
            neuron.value = inputArr[neuronPos];
        }
        for (final NormNeuron neuron : net.neuronList) {
            if (neuron.neuronType != NormNeuron.NeuronType.Input) {
                calcValue(neuron);
            }
        }
    }

    private static void calcValue(final NormNeuron neuron) {
        long sumValue = NormNeuron.NullValue;
        for (final NormSynapse synapse : neuron.parentSynapseList) {
            final NormNeuron parentNeuron = synapse.parentNeuron;
            // Value anhand aller parent Synapsen des Neuron:
            sumValue += (parentNeuron.value * synapse.weight) / NormNeuron.MaxValue;
        }
        neuron.value = calcActivation(sumValue);
    }

    /**
     * Lineare Aktivierungsfunktion (mit Clipping).
     * Hard Tanh (Hard Hyperbolic Tangent).
     */
    public static long calcActivation(final long value) {
        return Math.max(NormNeuron.MinValue, Math.min(NormNeuron.MaxValue, value));
    }
}
