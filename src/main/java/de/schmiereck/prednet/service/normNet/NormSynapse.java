package de.schmiereck.prednet.service.normNet;

public class NormSynapse {
    public final NormNeuron parentNeuron;
    public int weight;
    public final NormNeuron childNeuron;

    public NormSynapse(final NormNeuron parentNeuron, final int weight, final NormNeuron childNeuron) {
        this.parentNeuron = parentNeuron;
        this.weight = weight;
        this.childNeuron = childNeuron;
    }
}
