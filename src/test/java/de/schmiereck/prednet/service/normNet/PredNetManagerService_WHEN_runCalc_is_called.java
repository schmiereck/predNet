package de.schmiereck.prednet.service.normNet;

import de.schmiereck.prednet.service.CurveDto;
import de.schmiereck.prednet.service.CurveGeneratorService;
import de.schmiereck.prednet.service.PredNetManagerService;
import de.schmiereck.prednet.service.PredNetManagerServiceFactory;
import de.schmiereck.prednet.service.baseNet.BaseNetService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class PredNetManagerService_WHEN_runCalc_is_called {

    @Test
    void GIVEN_BigSawtooth_input_8_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigSawtooth;
        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        final boolean useOutputAsInput = false;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 50_000, 1L, 13_000);
    }

    @Test
    void GIVEN_BigSlowSine_input_8_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigSlowSine;
        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        final boolean useOutputAsInput = false;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 50_000, 1L, 9_000);
    }

    @Test
    void GIVEN_SmallSlowSine_input_8_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallSlowSine;
        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        final boolean useOutputAsInput = false;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 50_000, 1L, 9_000);
    }

    @Test
    void GIVEN_BigFastSine_input_8_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigFastSine;
        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        final boolean useOutputAsInput = false;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 50_000, 1L, 2_000);
    }

    @Test
    void GIVEN_SmallFastSine_input_8_loopback_None_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        final boolean useOutputAsInput = false;
        final NormNetService.LoopbackType loopbackType = NormNetService.LoopbackType.None;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput, loopbackType);

        assertCurveLearned(predNetManagerService, 50_000, 1L, 2_900);
    }

    @Test
    void GIVEN_SmallFastSine_input_8_loopback_Neuron_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        final boolean useOutputAsInput = false;
        final NormNetService.LoopbackType loopbackType = NormNetService.LoopbackType.Neuron;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput, loopbackType);

        assertCurveLearned(predNetManagerService, 50_000, 1L, 2_900);
    }

    @Test
    void GIVEN_ModulatedSine_input_16_hidden_6_useOutput_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.ModulatedSine;
        final int netInputCurveLength = 16;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 6;
        final boolean useOutputAsInput = true;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 150_000, 1L, 120_000);
    }

    @Test
    void GIVEN_Modulated2Sine_input_24_hidden_9_useOutput_THEN_curve_not_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.Modulated2Sine;
        final int netInputCurveLength = 24;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 9;
        final boolean useOutputAsInput = true;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 150_000, 30L, 150_000);
    }

    @Test
    void GIVEN_SmallFastSine_input_1_hidden_3_THEN_curve_not_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
        // Only one input value, so the net has to use its Memory to predict the curve.
        final int netInputCurveLength = 1;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        // Very bad without useOutputAsInput as Memory.
        final boolean useOutputAsInput = false;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 150_000, 30L, 150_000);
    }

    @Test
    void GIVEN_SmallFastSine_input_1_hidden_3_useOutput_THEN_curve_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
        // Only one input value, the network has to use its memory to predict the curve.
        final int netInputCurveLength = 1;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        // Very good with useOutputAsInput as Memory (Nearly impossible without).
        final boolean useOutputAsInput = true;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput);

        assertCurveLearned(predNetManagerService, 15_000, 1L, 5_000);
    }

    @Test
    void GIVEN_SmallFastSine_input_1_hidden_6_useLoopback_THEN_curve_not_learned() {
        final var predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();
        NormNetService.initNewRandomWithSeed(42L);

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.SmallFastSine;
        // Only one input value, the network has to use its memory to predict the curve.
        final int netInputCurveLength = 1;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 6;
        final boolean useOutputAsInput = false;
        final NormNetService.LoopbackType loopbackType = NormNetService.LoopbackType.Neuron;

        predNetManagerService.initNet(curveType, netInputCurveLength, netOutputCurveLength, hiddenLayerCount, useOutputAsInput, loopbackType);

        assertCurveLearned(predNetManagerService, 500_000, 1L, 500_000);
    }

    private static void assertCurveLearned(final PredNetManagerService predNetManagerService, final int maxIterationCount, final long expectedMse, final int expectedIterationCount) {
        long[] mseArr = new long[200];
        int nextMsePos = 0;
        int iterationPos;

        for (iterationPos = 0; iterationPos < maxIterationCount; iterationPos++) {
            predNetManagerService.runCalc();
            final CurveDto curveDto = predNetManagerService.retrieveCurve();

            mseArr[nextMsePos] = curveDto.mse();
            nextMsePos = (nextMsePos + 1) % mseArr.length;

            if (iterationPos > mseArr.length) {
                long averageMse = Arrays.stream(mseArr).sum() / mseArr.length;

                if (averageMse <= expectedMse) {
                    break;
                }
            }
        }

        // Expect that the curve is not learned in time?
        if (maxIterationCount == expectedIterationCount) {
            Assertions.assertTrue(iterationPos >= expectedIterationCount, "The curve was unexpected learned in time.");
        } else {
            Assertions.assertTrue(iterationPos < expectedIterationCount, "The curve was not learned in time.");
        }
    }
}
