package de.schmiereck.prednet.service.baseNet;

import de.schmiereck.prednet.service.normNet.NormNet;
import de.schmiereck.prednet.service.normNet.NormNetService;

import java.util.Random;

public abstract class BaseNetService {
    protected static Random random = new Random(42); // Fixer Seed für Reproduzierbarkeit

    public enum LoopbackType {
        None,
        Neuron,
        ParentNeuron
    }

    public static void initNewRandomWithSeed(final long seed) {
        random = new Random(seed);
    }

    public NormNet initNet(final int[] layerNeuronCounts) {
        final NormNetService.LoopbackType loopbackType = BaseNetService.LoopbackType.None;
        return initNet(layerNeuronCounts, loopbackType);
    }

    public abstract NormNet initNet(final int[] layerNeuronCounts, final LoopbackType loopbackType);

    public abstract long calcError(final NormNet net, final long[] targetOutputArr);

    public abstract void calcValue(final NormNet net, final long[] inputArr);

    public abstract void calcTrain(final NormNet net, final long learningRate);
}
