package de.schmiereck.prednet.service;

public class CurveGeneratorService {
    public enum CurveType {
        BigSawtooth,

        BigFastSine,
        SmallFastSine,

        BigSlowSine,
        SmallSlowSine,

        ModulatedSine
    }

    private static int[] precalcSawtoothCurveArr = new int[]
            {
                    0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
                    90, 80, 70, 60, 50, 40, 30, 20, 10,
                    0, -10, -20, -30, -40, -50, -60, -70, -80, -90, -100,
                    -90, -80, -70, -60, -50, -40, -30, -20, -10
            };

    public static int retrieveCurveValue(final CurveType curveType, final long timeStep) {
        return switch (curveType) {
            case BigSawtooth -> precalcSawtoothCurveArr[(int) (timeStep % precalcSawtoothCurveArr.length)];
            case BigFastSine -> (int) (100.0D * Math.sin((2.0D * Math.PI / 20.0D) * timeStep));
            case SmallFastSine -> (int) (40.0D * Math.sin((2.0D * Math.PI / 20.0D) * timeStep));
            case BigSlowSine -> (int) (100.0D * Math.sin((2.0D * Math.PI / 100.0D) * timeStep));
            case SmallSlowSine -> (int) (40.0D * Math.sin((2.0D * Math.PI / 100.0D) * timeStep));
            case ModulatedSine -> (int) (45.0D *
                        (
                            (Math.sin((2.0D * Math.PI /  20.0D) * timeStep)) +
                            (Math.sin((2.0D * Math.PI / 100.0D) * timeStep))
                        )
                    );
        };
    }
}
