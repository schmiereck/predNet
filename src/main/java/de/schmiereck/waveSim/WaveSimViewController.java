package de.schmiereck.waveSim;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;

public class WaveSimViewController {

    @FXML
    private WaveVisualizer waveVisualizer;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private Button resetButton;
    @FXML
    private Slider dampingSlider;

    private WaveSimulation simulation;
    private AnimationTimer animationTimer;

    private boolean running = false;

    @FXML
    public void initialize() {
        simulation = new WaveSimulation(200, dampingSlider.getValue());
        waveVisualizer.setSimulation(simulation);

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                simulation.step(0.1);
                waveVisualizer.draw();
            }
        };

        dampingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Unfortunately, we can't change the damping on the fly in the current WaveSimulation implementation.
            // We would need to add a setter for the damping factor.
        });

        waveVisualizer.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseInput);
        waveVisualizer.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMouseInput);
    }

    @FXML
    private void handleStartButton() {
        if (!running) {
            animationTimer.start();
            running = true;
        }
    }

    @FXML
    private void handleStopButton() {
        if (running) {
            animationTimer.stop();
            running = false;
        }
    }

    @FXML
    private void handleResetButton() {
        simulation.reset();
        waveVisualizer.draw();
    }

    private void handleMouseInput(MouseEvent event) {
        int x = (int) (event.getX() / waveVisualizer.getWidth() * simulation.getSize());
        simulation.addValue(x, 2.0);
    }
}

