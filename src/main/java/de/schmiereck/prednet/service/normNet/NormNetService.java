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

    public static long calcError(final NormNet net, final long[] targetOutputArr) {
        long totalError = NormNeuron.NullValue;

        for (int neuronPos = 0; neuronPos < net.outputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.outputNeuronList.get(neuronPos);
            final long targetOutput = targetOutputArr[neuronPos];
            neuron.error = targetOutput - neuron.value;

            // Akkumuliere quadratischen Fehler
            final long squaredError = (neuron.error * neuron.error);
            totalError += squaredError;
        }

        for (int neuronPos = net.neuronList.size() - 1; neuronPos >= 0; neuronPos--) {
            final NormNeuron neuron = net.neuronList.get(neuronPos);
            if (neuron.neuronType != NormNeuron.NeuronType.Output) {
                calcError(neuron);
            }
        }

        // Berechne Mean Squared Error
        final long mse = totalError / (net.outputNeuronList.size() * NormNeuron.MaxValue);
        return mse;
    }

    private static void calcError(final NormNeuron neuron) {
        neuron.error = NormNeuron.NullValue;
        for (final NormSynapse synapse : neuron.childSynapseList) {
            calcError(neuron, synapse);
        }
    }

    private static void calcError(final NormNeuron neuron, final NormSynapse synapse) {
        final NormNeuron childNeuron = synapse.childNeuron;
        final long propagatedError = (childNeuron.error * synapse.weight) / NormNeuron.MaxValue;
        neuron.error += propagatedError;
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

    public static void calcTrain(final NormNet net, final long learningRate) {
        // Aktualisiere alle Gewichte basierend auf den berechneten Fehlern
        for (final NormNeuron neuron : net.neuronList) {
            if (neuron.neuronType != NormNeuron.NeuronType.Input) {
                updateWeights(neuron, learningRate);
            }
        }
    }

    private static void updateWeights(final NormNeuron neuron, final long learningRate) {
        for (final NormSynapse synapse : neuron.parentSynapseList) {
            // Berechne Gewichtsanpassung: deltaWeight = learningRate * error * parentValue
            final long parentValue = synapse.parentNeuron.value;
            final long deltaWeight = (learningRate * neuron.error * parentValue) / (NormNeuron.MaxValue * NormNeuron.MaxValue);

            // Aktualisiere Gewicht
            synapse.weight = Math.max(NormNeuron.MinValue, Math.min(NormNeuron.MaxValue, synapse.weight + deltaWeight));
        }
    }

}
