package de.schmiereck.prednet.service.normNet;

import de.schmiereck.prednet.service.baseNet.BaseNetService;
import de.schmiereck.prednet.service.utils.NetServiceUtils;

public class NormNetService extends BaseNetService {

    @Override
    public NormNet initNet(final int[] layerNeuronCounts, final LoopbackType loopbackType) {
        return NetServiceUtils.getNormNet(rnd, layerNeuronCounts, loopbackType);
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

        // Berechne Mean Squared Error (MSE).
        return totalError / (net.outputNeuronList.size());
    }

    private static void calcError(final NormNeuron neuron) {
        final long oldParentError = neuron.error;
        long newParentError = NormNeuron.NullValue;
        for (final NormSynapse childSynapse : neuron.childSynapseList) {
            if (childSynapse.loopback) {
                newParentError += calcLoopbackError(neuron, childSynapse, oldParentError);
            } else {
                newParentError += calcError(neuron, childSynapse);
            }
        }
        // Multipliziere mit Ableitung der Aktivierungsfunktion
        neuron.error = (newParentError * calcActivationDerivative(neuron.value)) / NormNeuron.MaxValue;
    }

    private static long calcError(final NormNeuron neuron, final NormSynapse childSynapse) {
        final NormNeuron childNeuron = childSynapse.childNeuron;
        final long propagatedError = (childNeuron.error * childSynapse.weight) / NormNeuron.MaxValue;
        return propagatedError;
    }

    private static long calcLoopbackError(final NormNeuron neuron, final NormSynapse childSynapse, final long oldParentError) {
        final long propagatedError = (oldParentError * childSynapse.weight) / NormNeuron.MaxValue;
        return propagatedError;
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
    public void calcValue(final NormNet net, final CurvePoint[] inputArr) {
        for (int neuronPos = 0; neuronPos < net.inputNeuronList.size(); neuronPos++) {
            final NormNeuron neuron = net.inputNeuronList.get(neuronPos);
            neuron.value = inputArr[neuronPos].value();
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
        //neuron.lastValue = (neuronLastValue);
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
