module de.schmiereck.prednet {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.almasb.fxgl.all;

    opens de.schmiereck.prednet to javafx.fxml;
    exports de.schmiereck.prednet;
    opens de.schmiereck.hello to javafx.fxml;
    exports de.schmiereck.hello;
    opens de.schmiereck.waveSim to javafx.fxml;
    exports de.schmiereck.waveSim;
}