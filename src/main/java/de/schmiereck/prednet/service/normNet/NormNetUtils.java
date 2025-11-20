package de.schmiereck.prednet.service.normNet;

public abstract class NormNetUtils {
    private NormNetUtils(){}

    public static long calcValuePerc(final long perc) {
        return (perc * NormNeuron.MaxValue) / 100L;
    }
}
