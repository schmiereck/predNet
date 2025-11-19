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
                final NormNeuron neuron = new NormNeuron(NormNeuron.NullValue);

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

    public static void calcError(final NormNet net, final int[] targetOutputArr) {
        for (final NormNeuron neuron : net.neuronList) {
            neuron.error = 0;
        }

        for (int neuronPos = 0; neuronPos < net.outputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.outputNeuronList.get(neuronPos);
            final int targetOutput = targetOutputArr[neuronPos];
            neuron.error = targetOutput - neuron.value;
        }
        for (final NormNeuron neuron : net.outputNeuronList) {
            calcError(neuron, neuron.error);
        }
    }

    private static void calcError(final NormNeuron neuron, final int error) {
        for (final NormSynapse synapse : neuron.parentSynapseList) {
            calcError(neuron, synapse, error);
        }
    }

    private static void calcError(final NormNeuron neuron, final NormSynapse synapse, final int error) {
        final NormNeuron parentNeuron = synapse.parentNeuron;
        final int propagatedError = (error * synapse.weight) / NormNeuron.MaxValue;
        parentNeuron.error += propagatedError;

        calcError(parentNeuron, error);
    }

    public static void calcValue(final NormNet net) {
        for (final NormNeuron neuron : net.inputNeuronList) {
            calcValue(neuron);
        }
    }
    private static void calcValue(final NormNeuron neuron) {
        for (final NormSynapse synapse : neuron.childSynapseList) {
            final NormNeuron childNeuron = synapse.childNeuron;
            childNeuron.value += (neuron.value * synapse.weight) / NormNeuron.MaxValue;

            calcValue(childNeuron);
        }
    }
}
