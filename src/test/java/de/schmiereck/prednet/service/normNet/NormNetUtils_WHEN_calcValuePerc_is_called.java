package de.schmiereck.prednet.service.normNet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NormNetUtils_WHEN_calcValuePerc_is_called {

    @Test
    void GIVEN_0_percent_THEN_0L_returned() {
        final long value = NormNetUtils.calcValuePerc(0);
        Assertions.assertEquals(0L, value);
    }

    @Test
    void GIVEN_10_percent_THEN_100000L_returned() {
        final long value = NormNetUtils.calcValuePerc(10);
        Assertions.assertEquals(100000L, value);
        Assertions.assertEquals(NormNeuron.MaxValue / 10L, value);
    }

    @Test
    void GIVEN_50_percent_THEN_500000L_returned() {
        final long value = NormNetUtils.calcValuePerc(50);
        Assertions.assertEquals(500000L, value);
        Assertions.assertEquals(NormNeuron.MaxValue / 2L, value);
    }

    @Test
    void GIVEN_100_percent_THEN_1000000L_returned() {
        final long value = NormNetUtils.calcValuePerc(100);
        Assertions.assertEquals(1000000L, value);
        Assertions.assertEquals(NormNeuron.MaxValue, value);
    }
}
