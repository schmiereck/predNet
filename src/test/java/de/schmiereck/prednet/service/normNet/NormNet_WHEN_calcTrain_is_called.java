package de.schmiereck.prednet.service.normNet;

import de.schmiereck.prednet.service.baseNet.BaseNetService.CurvePoint;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.schmiereck.prednet.service.normNet.CurvePointTestUtils.createCurvePointArrByValueArr;

public class NormNet_WHEN_calcTrain_is_called {
    @Test
    void GIVEN_no_loopback_THEN_output_equal_input() {
        final NormNetService normNetService = new NormNetService();
        final NormNet net = new NormNet();

        final long inputValue = NormNetUtils.calcValuePerc(100L); // 100%
        final long weightValue = NormNetUtils.calcValuePerc(10L); // 10%

        final NormNeuron inputNeuron = new NormNeuron(NormNeuron.NeuronType.Input, NormNeuron.NullValue);
        final NormNeuron hiddenNeuron = new NormNeuron(NormNeuron.NeuronType.Hidden, NormNeuron.NullValue);
        final NormNeuron outputNeuron = new NormNeuron(NormNeuron.NeuronType.Output, NormNeuron.NullValue);

        net.neuronList.add(inputNeuron);
        net.inputNeuronList.add(inputNeuron);

        net.neuronList.add(hiddenNeuron);

        net.neuronList.add(outputNeuron);
        net.outputNeuronList.add(outputNeuron);

        //--------------------------------------------------------------------------------------------------------------
        final NormSynapse inputToHiddenSynapse = new NormSynapse(inputNeuron, weightValue, hiddenNeuron);
        inputToHiddenSynapse.loopback = false;

        inputNeuron.childSynapseList.add(inputToHiddenSynapse);
        hiddenNeuron.parentSynapseList.add(inputToHiddenSynapse);


        //--------------------------------------------------------------------------------------------------------------
        final NormSynapse hiddenToOutputSynapse = new NormSynapse(hiddenNeuron, weightValue, outputNeuron);
        hiddenToOutputSynapse.loopback = false;

        hiddenNeuron.childSynapseList.add(hiddenToOutputSynapse);
        outputNeuron.parentSynapseList.add(hiddenToOutputSynapse);

        //--------------------------------------------------------------------------------------------------------------

        final long[] inputArr = new long[] { inputValue };
        final CurvePoint[] inputPointArr = createCurvePointArrByValueArr(inputArr);

        final long[] targetOutputArr = new long[] { inputValue };

        //--------------------------------------------------------------------------------------------------------------
        final long learningRate = NormNetUtils.calcValuePerc(10L); // 10%

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputPointArr);
        normNetService.calcError(net, targetOutputArr);
        normNetService.calcTrain(net, learningRate);

        final long s1InputExpectedValue = inputValue;
        final long s1HiddenExpectedValue = (s1InputExpectedValue) * weightValue / NormNeuron.MaxValue;
        final long s1OutputExpectedValue = (s1HiddenExpectedValue) * weightValue / NormNeuron.MaxValue;

        Assertions.assertEquals(s1InputExpectedValue, inputNeuron.value);
        Assertions.assertEquals(0L, inputNeuron.error);

        Assertions.assertEquals(109_900L, inputToHiddenSynapse.weight);

        Assertions.assertEquals(s1HiddenExpectedValue, hiddenNeuron.value);
        Assertions.assertEquals(99_000L, hiddenNeuron.error);

        Assertions.assertEquals(109_900L, hiddenToOutputSynapse.weight);

        Assertions.assertEquals(s1OutputExpectedValue, outputNeuron.value);
        Assertions.assertEquals(990_000L, outputNeuron.error);

        //--------------------------------------------------------------------------------------------------------------
    }

    @Test
    void GIVEN_with_loopback_THEN_output_equal_input() {
        final NormNetService normNetService = new NormNetService();
        final NormNet net = new NormNet();

        final long inputValue = NormNetUtils.calcValuePerc(100L); // 100%
        final long weightValue = NormNetUtils.calcValuePerc(10L); // 10%

        final NormNeuron inputNeuron = new NormNeuron(NormNeuron.NeuronType.Input, NormNeuron.NullValue);
        final NormNeuron hiddenNeuron = new NormNeuron(NormNeuron.NeuronType.Hidden, NormNeuron.NullValue);
        final NormNeuron outputNeuron = new NormNeuron(NormNeuron.NeuronType.Output, NormNeuron.NullValue);

        net.neuronList.add(inputNeuron);
        net.inputNeuronList.add(inputNeuron);

        net.neuronList.add(hiddenNeuron);

        net.neuronList.add(outputNeuron);
        net.outputNeuronList.add(outputNeuron);

        //--------------------------------------------------------------------------------------------------------------
        final NormSynapse inputToHiddenSynapse = new NormSynapse(inputNeuron, weightValue, hiddenNeuron);
        inputToHiddenSynapse.loopback = false;

        inputNeuron.childSynapseList.add(inputToHiddenSynapse);
        hiddenNeuron.parentSynapseList.add(inputToHiddenSynapse);


        //--------------------------------------------------------------------------------------------------------------
        final NormSynapse hiddenToOutputSynapse = new NormSynapse(hiddenNeuron, weightValue, outputNeuron);
        hiddenToOutputSynapse.loopback = false;

        hiddenNeuron.childSynapseList.add(hiddenToOutputSynapse);
        outputNeuron.parentSynapseList.add(hiddenToOutputSynapse);

        //--------------------------------------------------------------------------------------------------------------
        final NormSynapse hiddenToHiddenSynapse = new NormSynapse(hiddenNeuron, weightValue, hiddenNeuron);
        hiddenToHiddenSynapse.loopback = true;

        hiddenNeuron.childSynapseList.add(hiddenToHiddenSynapse);
        hiddenNeuron.parentSynapseList.add(hiddenToHiddenSynapse);

        //--------------------------------------------------------------------------------------------------------------

        final long[] inputArr = new long[] { inputValue };
        final CurvePoint[] inputPointArr = createCurvePointArrByValueArr(inputArr);

        final long[] targetOutputArr = new long[] { inputValue };

        //--------------------------------------------------------------------------------------------------------------
        final long learningRate = NormNetUtils.calcValuePerc(10L); // 10%

        //--------------------------------------------------------------------------------------------------------------
        normNetService.calcValue(net, inputPointArr);
        normNetService.calcError(net, targetOutputArr);
        normNetService.calcTrain(net, learningRate);

        final long s1InputExpectedValue = inputValue;
        final long s1HiddenExpectedValue = (s1InputExpectedValue) * weightValue / NormNeuron.MaxValue;
        final long s1OutputExpectedValue = (s1HiddenExpectedValue) * weightValue / NormNeuron.MaxValue;

        Assertions.assertEquals(s1InputExpectedValue, inputNeuron.value);
        Assertions.assertEquals(0L, inputNeuron.error);

        //Assertions.assertEquals(109_900L, inputToHiddenSynapse.weight);
        Assertions.assertEquals(109_900L, inputToHiddenSynapse.weight);

        Assertions.assertEquals(s1HiddenExpectedValue, hiddenNeuron.value);
        //Assertions.assertEquals(99_000L, hiddenNeuron.error);
        Assertions.assertEquals(99_000L, hiddenNeuron.error);

        Assertions.assertEquals(100_495L, hiddenToHiddenSynapse.weight);

        Assertions.assertEquals(109_900L, hiddenToOutputSynapse.weight);

        Assertions.assertEquals(s1OutputExpectedValue, outputNeuron.value);
        Assertions.assertEquals(990_000L, outputNeuron.error);

        //--------------------------------------------------------------------------------------------------------------
    }
}
