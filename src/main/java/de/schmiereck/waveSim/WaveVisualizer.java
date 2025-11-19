package de.schmiereck.waveSim;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class WaveVisualizer extends Canvas {

    private WaveSimulation simulation;

    public WaveVisualizer(double width, double height) {
        super(width, height);
    }

    public WaveVisualizer() {
        super();
    }

    public void setSimulation(WaveSimulation simulation) {
        this.simulation = simulation;
    }

    public void draw() {
        if (simulation == null) {
            return;
        }

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        double[] pos = simulation.getPosition();
        int size = simulation.getSize();
        double barWidth = getWidth() / size;

        for (int i = 0; i < size; i++) {
            double value = pos[i];
            double barHeight = value * getHeight() / 2;
            double x = i * barWidth;
            double y;

            if (value >= 0) {
                y = getHeight() / 2 - barHeight;
                gc.setFill(Color.BLUE);
            } else {
                y = getHeight() / 2;
                barHeight = -barHeight;
                gc.setFill(Color.RED);
            }

            gc.fillRect(x, y, barWidth, barHeight);
        }
    }
}
