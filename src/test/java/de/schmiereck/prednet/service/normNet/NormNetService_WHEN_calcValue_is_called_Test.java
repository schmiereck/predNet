package de.schmiereck.prednet.service.normNet;

import de.schmiereck.prednet.service.baseNet.BaseNetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static de.schmiereck.prednet.service.normNet.CurvePointTestUtils.createCurvePointArrByValueArr;

public class NormNetService_WHEN_calcValue_is_called_Test {


    @Test
    void GIVEN_2_layer_net_THEN_output_calculated() {
        final int[] layerNeuronCounts = new int[]{2, 2};
        final NormNetService normNetService = new NormNetService();
        final NormNet net = normNetService.initNet(layerNeuronCounts);

        net.neuronList.get(2).parentSynapseList.get(0).weight = 100_000; // 0.1 in NormNeuron representation
        net.neuronList.get(2).parentSynapseList.get(1).weight = 200_000; // 0.2 in NormNeuron representation
        net.neuronList.get(2).parentSynapseList.get(2).weight = 0; // Bias.

        net.neuronList.get(3).parentSynapseList.get(0).weight = 200_000; // 0.2 in NormNeuron representation
        net.neuronList.get(3).parentSynapseList.get(1).weight = 400_000; // 0.4 in NormNeuron representation
        net.neuronList.get(3).parentSynapseList.get(2).weight = 100_000; // Bias.

        final long[] inputArr = new long[]{ 500, 1_000 };
        final BaseNetService.CurvePoint[] inputPointArr = createCurvePointArrByValueArr(inputArr);

        normNetService.calcValue(net, inputPointArr);

        // Max	1000000
        // Bias	1000000
        // input	500		        	1000
        // weight	100000	200000	0	200000	400000	100000
        //      	50	    200	    0	100	    400	    100000
        // output	250			        100500

        final long l0n0e = 500; // input 500
        final long l0n1e = 1_000; // input 1_000
        final long l1n0e =
                ((l0n0e * 100_000) / NormNeuron.MaxValue) +
                ((l0n1e * 200_000) / NormNeuron.MaxValue) +
                ((NormNeuron.MaxValue * 0) / NormNeuron.MaxValue); // 500 * 0.1 + 1_000 * 0.2 + MaxValue * 0.0  = 250
        final long l1n1o0 = ((l0n0e * 200_000) / NormNeuron.MaxValue);
        final long l1n1o1 = ((l0n1e * 400_000) / NormNeuron.MaxValue);
        final long l1n1o2 = (int)((NormNeuron.MaxValue * 100_000) / NormNeuron.MaxValue);
        final long l1n1e =
                l1n1o0 +
                l1n1o1 +
                l1n1o2; // 500 * 0.2 + 1_000 * 0.4 + MaxValue * 0.1  = 100500
        final long[] expectedOutputArr = new long[]{ l1n0e, l1n1e };

        Assertions.assertEquals(expectedOutputArr[0], net.outputNeuronList.get(0).value, "Output Neuron 0.");
        Assertions.assertEquals(expectedOutputArr[1], net.outputNeuronList.get(1).value, "Output Neuron 1.");
    }
}
