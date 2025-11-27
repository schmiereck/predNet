package de.schmiereck.prednet.service.normNet;

import de.schmiereck.prednet.service.baseNet.BaseNetService;

import java.util.Random;

public class NormNetService extends BaseNetService {

    @Override
    public NormNet initNet(final int[] layerNeuronCounts, final LoopbackType loopbackType) {
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
                        final NormSynapse synapse = new NormSynapse(parentLayerNeuron, calcInitWeight(inputCount), neuron);
                        neuron.parentSynapseList.add(synapse);
                        parentLayerNeuron.childSynapseList.add(synapse);
                    }
                    if ((loopbackType != LoopbackType.None) && (neuronType == NormNeuron.NeuronType.Hidden)) {
                        // Add loopback memory synapse.
                        switch (loopbackType) {
                            case Neuron -> {
                                final NormSynapse loopbackSynapse = new NormSynapse(neuron, calcInitWeight(inputCount), neuron);
                                loopbackSynapse.loopback = true;
                                neuron.parentSynapseList.add(loopbackSynapse);
                                neuron.childSynapseList.add(loopbackSynapse);
                            }
                            case ParentNeuron -> {
                                final NormNeuron parentLayerNeuron = parentLayerNeuronArr[neuronPos];
                                final NormSynapse loopbackSynapse = new NormSynapse(parentLayerNeuron, calcInitWeight(inputCount), neuron);
                                loopbackSynapse.loopback = true;
                                parentLayerNeuron.parentSynapseList.add(loopbackSynapse);
                                neuron.childSynapseList.add(loopbackSynapse);
                            }
                        }
                        //final NormSynapse loopbackSynapse = new NormSynapse(neuron, calcInitWeight(inputCount), neuron);
                        final NormNeuron parentLayerNeuron = parentLayerNeuronArr[neuronPos];
                        final NormSynapse loopbackSynapse = new NormSynapse(parentLayerNeuron, calcInitWeight(inputCount), neuron);
                        loopbackSynapse.loopback = true;
                        //neuron.parentSynapseList.add(loopbackSynapse);
                        parentLayerNeuron.parentSynapseList.add(loopbackSynapse);
                        neuron.childSynapseList.add(loopbackSynapse);
                    }
                    // Add bias synapse.
                    final NormSynapse synapse = new NormSynapse(net.biasNeuron, calcInitWeight(inputCount), neuron);
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
    private static long calcInitWeight() {
        // Zufallswert zwischen -0.1 und +0.1 (10% von MaxValue)
        final double randomValue = (random.nextDouble() * 2.0 - 1.0) * 0.1;
        return (long) (randomValue * NormNeuron.MaxValue);
    }

    /**
     * Alternative: He-Initialisierung (besser für ReLU-artige Funktionen)
     */
    private static long calcInitWeight(final int inputCount) {
        // He-Initialisierung: sqrt(2/n)
        final double scale = Math.sqrt(2.0 / inputCount);
        final double randomValue = random.nextGaussian() * scale;
        return (long) Math.max(NormNeuron.MinValue,
                Math.min(NormNeuron.MaxValue,
                        randomValue * NormNeuron.MaxValue));
    }

    @Override
    public long calcError(final NormNet net, final long[] targetOutputArr) {
        long totalError = NormNeuron.NullValue;

        for (int neuronPos = 0; neuronPos < net.outputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.outputNeuronList.get(neuronPos);
            final long targetOutput = targetOutputArr[neuronPos];
            neuron.error = targetOutput - neuron.value;

            // Akkumuliere quadratischen Fehler
            //final long squaredError = (neuron.error * neuron.error);// / NormNeuron.MaxValue;
            final long squaredError = (neuron.error * neuron.error) / NormNeuron.MaxValue;
            totalError += squaredError;
        }

        for (int neuronPos = net.neuronList.size() - 1; neuronPos >= 0; neuronPos--) {
            final NormNeuron neuron = net.neuronList.get(neuronPos);
            if (neuron.neuronType != NormNeuron.NeuronType.Output) {
                calcError(neuron);
            }
        }

        // Berechne Mean Squared Error
        final long mse = totalError / (net.outputNeuronList.size());
        return mse;
    }

    private static void calcError(final NormNeuron neuron) {
        neuron.error = NormNeuron.NullValue;
        for (final NormSynapse synapse : neuron.childSynapseList) {
            calcError(neuron, synapse);
        }
        // Multipliziere mit Ableitung der Aktivierungsfunktion
        neuron.error = (neuron.error * calcActivationDerivative(neuron.value)) / NormNeuron.MaxValue;
    }

    private static void calcError(final NormNeuron neuron, final NormSynapse synapse) {
        final NormNeuron childNeuron = synapse.childNeuron;
        final long propagatedError = (childNeuron.error * synapse.weight) / NormNeuron.MaxValue;
        neuron.error += propagatedError;
    }

    /**
     * Ableitung der Hard Tanh Aktivierungsfunktion.
     * Gibt 1 (= MaxValue) zurück wenn nicht gesättigt, sonst 0.
     */
    private static long calcActivationDerivative(final long value) {
        if (value <= NormNeuron.MinValue || value >= NormNeuron.MaxValue) {
            return NormNeuron.NullValue; // Gesättigt → Gradient = 0
        }
        return NormNeuron.MaxValue; // Linear → Gradient = 1
    }

    @Override
    public void calcValue(final NormNet net, final long[] inputArr) {
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
            final long parentValue;
            if (synapse.loopback) {
                // Verwende gespeicherten Wert für Loopback Synapse.
                parentValue = parentNeuron.lastValue;
            } else {
                parentValue = parentNeuron.value;
            }
            // Value anhand aller parent Synapsen des Neuron:
            sumValue += (parentValue * synapse.weight) / NormNeuron.MaxValue;
        }
        final long neuronLastValue = neuron.value;
        neuron.value = calcActivation(sumValue);
        neuron.lastValue = (neuronLastValue + neuron.value) / 2; // Durchschnittswert als lastValue speichern
    }

    /**
     * Lineare Aktivierungsfunktion (mit Clipping).
     * Hard Tanh (Hard Hyperbolic Tangent).
     */
    private static long calcActivation(final long value) {
        return Math.max(NormNeuron.MinValue, Math.min(NormNeuron.MaxValue, value));
    }

    @Override
    public void calcTrain(final NormNet net, final long learningRate) {
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
            //final long parentValue = synapse.parentNeuron.value;
            final long parentValue = synapse.loopback ? synapse.parentNeuron.lastValue : synapse.parentNeuron.value;
            //final long deltaWeight = (learningRate * neuron.error * parentValue) / (NormNeuron.MaxValue * NormNeuron.MaxValue);
            // Erst error × parentValue, dann × learningRate
            final long errorTimesValue = (neuron.error * parentValue) / NormNeuron.MaxValue;
            final long deltaWeight = (learningRate * errorTimesValue) / NormNeuron.MaxValue;

            // Aktualisiere Gewicht
            synapse.weight = Math.max(NormNeuron.MinValue, Math.min(NormNeuron.MaxValue, synapse.weight + deltaWeight));
        }
    }

}
