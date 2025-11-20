package de.schmiereck.prednet.service.normNet;

import java.util.ArrayList;
import java.util.List;

public class NormNeuron {
    public static final long MaxValue = 1_000_000L;
    public static final long NullValue = 0L;
    public static final long MinValue = -MaxValue;

    public enum NeuronType { Input, Hidden, Output, Bias }

    public final NeuronType neuronType;
    public long value;
    public long error;
    public final List<NormSynapse> parentSynapseList = new ArrayList<>();
    public final List<NormSynapse> childSynapseList = new ArrayList<>();

    public NormNeuron(final NeuronType neuronType, final long value) {
        this.neuronType = neuronType;
        this.value = value;
    }
}
