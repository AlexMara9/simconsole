package org.simconsole.simconsole.models;
/**
 * Audio effect processor implementing a flanger effect with low-frequency oscillation (LFO), delay buffer, and optional peaking bell filter.
 */
public class Flanger {
    private final double[] delayBuffer;
    private int writeIndex;
    private double lfoPhase;
    private final double sampleRate;
    private volatile boolean enabled = false;
    private volatile double depth = 0.5;
    private volatile double rate = 0.5;
    private volatile double feedback = 0.6;
    private volatile double wet = 0.0;
    private volatile boolean bellEnabled = true;
    private final BiquadFilter bellFilter;
    public Flanger(double sampleRate) {
        this.sampleRate = sampleRate;
        this.delayBuffer = new double[4096];
        this.writeIndex = 0;
        this.lfoPhase = 0.0;
        this.bellFilter = new BiquadFilter(BiquadFilter.FilterType.PEAKING, sampleRate, 1000.0, 1.5, 8.0);
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void setWet(double wet) {
        this.wet = Math.max(0.0, Math.min(1.0, wet));
    }
    public void setBellEnabled(boolean enabled) {
        this.bellEnabled = enabled;
    }
    public double process(double input) {
        if (!enabled || wet <= 0.001) {
            delayBuffer[writeIndex] = input;
            writeIndex = (writeIndex + 1) % delayBuffer.length;
            return input;
        }
        lfoPhase += rate / sampleRate;
        if (lfoPhase >= 1.0) {
            lfoPhase -= 1.0;
        }
        double lfoVal = Math.sin(2.0 * Math.PI * lfoPhase);
        double delayMs = 5.0 + 4.0 * depth * lfoVal;
        double delaySamples = (delayMs * sampleRate) / 1000.0;
        double readIndex = writeIndex - delaySamples;
        if (readIndex < 0) {
            readIndex += delayBuffer.length;
        }
        int idx0 = (int) readIndex;
        int idx1 = (idx0 + 1) % delayBuffer.length;
        double frac = readIndex - idx0;
        double delayedSample = delayBuffer[idx0] * (1.0 - frac) + delayBuffer[idx1] * frac;
        double feedforward = input + delayedSample * feedback;
        feedforward = Math.max(-1.0, Math.min(1.0, feedforward));
        delayBuffer[writeIndex] = feedforward;
        writeIndex = (writeIndex + 1) % delayBuffer.length;
        double wetSignal = delayedSample;
        if (bellEnabled) {
            double bellFreq = 400.0 * Math.pow(8.0, (lfoVal + 1.0) / 2.0);
            bellFilter.setFrequency(bellFreq);
            wetSignal = bellFilter.process(wetSignal);
        }
        return input * (1.0 - wet) + wetSignal * wet;
    }
}
