package de.schmiereck.prednet.service.baseNet;

import de.schmiereck.prednet.service.normNet.NormNet;
import de.schmiereck.prednet.service.normNet.NormNetService;

import java.util.Random;

public abstract class BaseNetService {
    protected static Random rnd = new Random(42); // Fixer Seed für Reproduzierbarkeit

    public enum LoopbackType {
        None,
        Neuron,
        ParentNeuron,
        AllParentsNeuron1,
        AllParentsNeuron2,
        TopParentNeuron
    }

    public record CurvePoint(long timePos, long value) {
    }

    public static void initNewRandomWithSeed(final long seed) {
        rnd = new Random(seed);
    }

    public NormNet initNet(final int[] layerNeuronCounts) {
        final NormNetService.LoopbackType loopbackType = BaseNetService.LoopbackType.None;
        return initNet(layerNeuronCounts, loopbackType);
    }

    public abstract NormNet initNet(final int[] layerNeuronCounts, final LoopbackType loopbackType);

    public abstract long calcError(final NormNet net, final long[] targetOutputArr);

    public abstract void calcValue(final NormNet net, final CurvePoint[] inputArr);

    public abstract void calcTrain(final NormNet net, final long learningRate);
}
