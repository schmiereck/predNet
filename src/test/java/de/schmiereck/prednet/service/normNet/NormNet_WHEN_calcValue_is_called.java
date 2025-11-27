package de.schmiereck.prednet.service.normNet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NormNet_WHEN_calcValue_is_called {
    @Test
    void GIVEN_weight_1000L_no_loopback_THEN_100L_output() {
        final NormNetService normNetService = new NormNetService();
        final NormNet net = new NormNet();

        final NormNeuron neuron = new NormNeuron(NormNeuron.NeuronType.Hidden, NormNeuron.NullValue);

        final long weightValue = 1_000L;
        final long inputValue = 100_000L;

        final NormSynapse synapse = new NormSynapse(neuron, weightValue, neuron);
        synapse.loopback = false;

        neuron.parentSynapseList.add(synapse);
        neuron.childSynapseList.add(synapse);

        net.neuronList.add(neuron);

        neuron.value = inputValue;

        final long[] inputArr = new long[] { };

        //--------------------------------------------------------------------------------------------------------------
        final long s0ExpectedValue = inputValue;
        final long s0ExpectedLastValue = 0L;

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputArr);

        final long s1ExpectedValue = (s0ExpectedValue) * weightValue / NormNeuron.MaxValue;
        final long s1ExpectedLastValue = ((s0ExpectedValue + s1ExpectedValue) / 2L);

        Assertions.assertEquals(s1ExpectedValue, neuron.value);
        Assertions.assertEquals(s1ExpectedLastValue, neuron.lastValue);

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputArr);

        final long s2ExpectedValue = (s1ExpectedValue) * weightValue / NormNeuron.MaxValue;
        final long s2ExpectedLastValue = ((s1ExpectedValue + s2ExpectedValue) / 2L);

        Assertions.assertEquals(s2ExpectedValue, neuron.value);
        Assertions.assertEquals(s2ExpectedLastValue, neuron.lastValue);
    }

    @Test
    void GIVEN_weight_1000L_loopback_THEN_100L_output() {
        final NormNetService normNetService = new NormNetService();
        final NormNet net = new NormNet();

        final NormNeuron neuron = new NormNeuron(NormNeuron.NeuronType.Hidden, NormNeuron.NullValue);

        final long weightValue = 100_000L;
        final long inputValue = 100_000L;

        final NormSynapse synapse = new NormSynapse(neuron, weightValue, neuron);
        synapse.loopback = true;

        neuron.parentSynapseList.add(synapse);
        neuron.childSynapseList.add(synapse);

        net.neuronList.add(neuron);

        neuron.value = inputValue;

        final long[] inputArr = new long[] { };

        //--------------------------------------------------------------------------------------------------------------
        final long s0ExpectedValue = inputValue;
        final long s0ExpectedLastValue = 0L;

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputArr);

        final long s1ExpectedValue = (s0ExpectedLastValue) * weightValue / NormNeuron.MaxValue;;
        final long s1ExpectedLastValue = ((s0ExpectedValue + s1ExpectedValue) / 2L);

        Assertions.assertEquals(s1ExpectedValue, neuron.value);
        Assertions.assertEquals(s1ExpectedLastValue, neuron.lastValue);

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputArr);

        final long s2ExpectedValue = (s1ExpectedLastValue) * weightValue / NormNeuron.MaxValue;;
        final long s2ExpectedLastValue = ((s1ExpectedValue + s2ExpectedValue) / 2L);

        Assertions.assertEquals(s2ExpectedValue, neuron.value);
        Assertions.assertEquals(s2ExpectedLastValue, neuron.lastValue);

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputArr);

        final long s3ExpectedValue = (s2ExpectedLastValue) * weightValue / NormNeuron.MaxValue;;
        final long s3ExpectedLastValue = ((s2ExpectedValue + s3ExpectedValue) / 2L);

        Assertions.assertEquals(s3ExpectedValue, neuron.value);
        Assertions.assertEquals(s3ExpectedLastValue, neuron.lastValue);

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputArr);

        final long s4ExpectedValue = (s3ExpectedLastValue) * weightValue / NormNeuron.MaxValue;;
        final long s4ExpectedLastValue = ((s3ExpectedValue + s4ExpectedValue) / 2L);

        Assertions.assertEquals(s4ExpectedValue, neuron.value);
        Assertions.assertEquals(s4ExpectedLastValue, neuron.lastValue);
    }
}
