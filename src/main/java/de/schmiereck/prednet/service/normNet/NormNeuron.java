package de.schmiereck.prednet.service.normNet;

import java.util.ArrayList;
import java.util.List;

public class NormNeuron {
    public static final int MaxValue = 1_000_000;
    public static final int NullValue = 0;
    public static final int MinValue = -MaxValue;

    public int value;
    public int error;
    public final List<NormSynapse> parentSynapseList = new ArrayList<>();
    public final List<NormSynapse> childSynapseList = new ArrayList<>();

    public NormNeuron(final int value) {
        this.value = value;
    }
}
