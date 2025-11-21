package de.schmiereck.prednet;

import de.schmiereck.prednet.service.PredNetManagerService;
import de.schmiereck.prednet.service.PredNetManagerServiceFactory;
import de.schmiereck.prednet.service.PredNetService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PredNetFxApp extends Application {

    private PredNetManagerService predNetManagerService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.predNetManagerService = PredNetManagerServiceFactory.retrievePredNetManagerService();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("prednet-view.fxml"));
        //FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(this.chartResource.getURL()));
        //final FXMLLoader fxmlLoader = new FXMLLoader(PredNetFxApp.class.getResource("classpath:/prednet-view.fxml"));
        Parent root = fxmlLoader.load();
        PredNetViewController controller = fxmlLoader.getController();
        controller.init(this.predNetManagerService);

        primaryStage.setTitle("PredNet Anzeige");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

