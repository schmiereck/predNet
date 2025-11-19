package de.schmiereck.waveSim;

public class WaveSimulation {

    private final int size;
    private final double d; // Damping factor

    private double[] pos;
    private double[] vel;
    private double[] acc;

    public WaveSimulation(int size, double d) {
        this.size = size;
        this.d = d;
        this.pos = new double[size];
        this.vel = new double[size];
        this.acc = new double[size];
    }

    public void reset() {
        for (int i = 0; i < size; i++) {
            pos[i] = 0.0;
            vel[i] = 0.0;
            acc[i] = 0.0;
        }
    }

    public void setValue(int x, double value) {
        if (x >= 0 && x < size) {
            this.pos[x] = value;
        }
    }

    public void addValue(int x, double value) {
        if (x >= 0 && x < size) {
            this.pos[x] += value;
        }
    }

    public void step(double dt) {
        // Calculate forces and acceleration
        double[] force = new double[size];
        for (int i = 0; i < size; i++) {
            // Interaction with neighbors
            double prev_pos = pos[(i - 1 + size) % size];
            double next_pos = pos[(i + 1) % size];
            force[i] = (prev_pos - pos[i]) + (next_pos - pos[i]);
        }

        for (int i = 0; i < size; i++) {
            acc[i] = force[i] - vel[i] * this.d;
        }

        // Update velocity and position
        for (int i = 0; i < size; i++) {
            vel[i] += acc[i] * dt;
            pos[i] += vel[i] * dt;
        }
    }

    public double[] getPosition() {
        return pos;
    }

    public int getSize() {
        return size;
    }
}

