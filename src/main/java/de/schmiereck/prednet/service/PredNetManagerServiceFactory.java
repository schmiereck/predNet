package de.schmiereck.prednet.service;

import java.util.Objects;

public abstract class PredNetManagerServiceFactory {
    private static PredNetManagerService predNetManagerService;

    private PredNetManagerServiceFactory() {
    }

    public synchronized static PredNetManagerService retrievePredNetManagerService() {
        if (Objects.isNull(predNetManagerService)) {
            predNetManagerService = new PredNetManagerService();
        }
        return predNetManagerService;
    }
}
