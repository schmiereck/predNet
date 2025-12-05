package de.schmiereck.prednet;

import de.schmiereck.prednet.service.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PredNetMain {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("PredNet V1.0.0");

        final CurveGeneratorService.CurveType curveType = CurveGeneratorService.CurveType.BigSawtooth;

        final PredNetManagerService predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();

        final int netInputCurveLength = 8;
        final int netOutputCurveLength = 6;
        final int hiddenLayerCount = 3;
        final int hiddenLayerNeuronCount = netInputCurveLength * 2;
        final boolean useOutputAsInput = false;
        predNetManagerService.initNet(curveType,
                netInputCurveLength, netOutputCurveLength, hiddenLayerCount, hiddenLayerNeuronCount,
                useOutputAsInput);

        predNetManagerService.runCalc(); // einmal initial berechnen

        // Hintergrund-Thread: ruft alle 10 ms calc() auf
        final ScheduledExecutorService calcScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PredNetCalc");
            t.setDaemon(true); // Daemon, damit JVM bei Ende des Hauptthreads beenden kann
            return t;
        });
        calcScheduler.scheduleAtFixedRate(() -> predNetManagerService.runCalc(), 0, 10, TimeUnit.MILLISECONDS);

        // Zweiter Thread: zeigt alle 40 ms die Kurve an
        final ScheduledExecutorService showScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PredNetShowCurve");
            t.setDaemon(true); // ebenfalls Daemon
            return t;
        });
        showScheduler.scheduleAtFixedRate(() -> showCurve(predNetManagerService), 0, 40, TimeUnit.MILLISECONDS);

        // Hauptthread bleibt eine Zeit lang aktiv, damit Ausgaben sichtbar sind
        Thread.sleep(2000); // 2 Sekunden Demo-Lauf

        // Scheduler beenden
        calcScheduler.shutdownNow();
        showScheduler.shutdownNow();
    }

    private static void showCurve(final PredNetManagerService predNetManagerService) {
        final CurveDto curveDto = predNetManagerService.retrieveCurve();
        final long[] inputArr = curveDto.inputHistorieCurveArr();
        for (int xPos = 0; xPos < inputArr.length; xPos++) {
            System.out.printf("%3d ", inputArr[xPos]);
        }
        System.out.print(" |  ");
        final long[] outputArr = curveDto.outputHistorieCurveArr();
        for (int xPos = 0; xPos < outputArr.length; xPos++) {
            System.out.printf("%3d ", outputArr[xPos]);
        }
        //System.out.printf(" : %s", curveDto.outputCurveArr());
        System.out.println();
    }
}
