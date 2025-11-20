package de.schmiereck.prednet.service.normNet;

import java.util.ArrayList;
import java.util.List;

public class NormNet {
    public final List<NormNeuron> inputNeuronList = new ArrayList<>();
    public final List<NormNeuron> outputNeuronList = new ArrayList<>();
    public final List<NormNeuron> neuronList = new ArrayList<>();
    public final NormNeuron biasNeuron = new NormNeuron(NormNeuron.NeuronType.Bias, NormNeuron.MaxValue);
}
