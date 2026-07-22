package org.simconsole.simconsole.models;
public class BiquadFilter {
    public enum FilterType {
        LOW_SHELF,
        PEAKING,
        HIGH_SHELF
    }
    private double a1, a2, b0, b1, b2;
    private double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
    private double sampleRate;
    private FilterType type;
    private double frequency;
    private double q;
    private double gainDB;
    public BiquadFilter(FilterType type, double sampleRate, double frequency, double q, double gainDB) {
        this.sampleRate = sampleRate;
        this.type = type;
        this.frequency = frequency;
        this.q = q;
        setGain(gainDB);
    }
    public void setFrequency(double frequency) {
        this.frequency = frequency;
        updateCoefficients();
    }
    public void setQ(double q) {
        this.q = q;
        updateCoefficients();
    }
    public void setGain(double gainDB) {
        this.gainDB = gainDB;
        updateCoefficients();
    }
    public double getGain() {
        return this.gainDB;
    }
    private void updateCoefficients() {
        double w0 = 2 * Math.PI * frequency / sampleRate;
        double alpha = Math.sin(w0) / (2 * q);
        double A = Math.pow(10, gainDB / 40.0);
        double a0_temp = 1.0;
        switch (type) {
            case LOW_SHELF:
                b0 = A * ((A + 1) - (A - 1) * Math.cos(w0) + 2 * Math.sqrt(A) * alpha);
                b1 = 2 * A * ((A - 1) - (A + 1) * Math.cos(w0));
                b2 = A * ((A + 1) - (A - 1) * Math.cos(w0) - 2 * Math.sqrt(A) * alpha);
                a0_temp = (A + 1) + (A - 1) * Math.cos(w0) + 2 * Math.sqrt(A) * alpha;
                a1 = -2 * ((A - 1) + (A + 1) * Math.cos(w0));
                a2 = (A + 1) + (A - 1) * Math.cos(w0) - 2 * Math.sqrt(A) * alpha;
                break;
            case PEAKING:
                b0 = 1 + alpha * A;
                b1 = -2 * Math.cos(w0);
                b2 = 1 - alpha * A;
                a0_temp = 1 + alpha / A;
                a1 = -2 * Math.cos(w0);
                a2 = 1 - alpha / A;
                break;
            case HIGH_SHELF:
                b0 = A * ((A + 1) + (A - 1) * Math.cos(w0) + 2 * Math.sqrt(A) * alpha);
                b1 = -2 * A * ((A - 1) + (A + 1) * Math.cos(w0));
                b2 = A * ((A + 1) + (A - 1) * Math.cos(w0) - 2 * Math.sqrt(A) * alpha);
                a0_temp = (A + 1) - (A - 1) * Math.cos(w0) + 2 * Math.sqrt(A) * alpha;
                a1 = 2 * ((A - 1) - (A + 1) * Math.cos(w0));
                a2 = (A + 1) - (A - 1) * Math.cos(w0) - 2 * Math.sqrt(A) * alpha;
                break;
        }
        b0 /= a0_temp;
        b1 /= a0_temp;
        b2 /= a0_temp;
        a1 /= a0_temp;
        a2 /= a0_temp;
    }
    public double process(double input) {
        double output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
        if (Math.abs(output) < 1.0e-15) {
            output = 0.0;
        }
        x2 = x1;
        x1 = input;
        y2 = y1;
        y1 = output;
        return output;
    }
}