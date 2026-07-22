package org.simconsole.simconsole.models;

public class Flanger {
    private final double[] delayBuffer;
    private int writeIndex;
    private double lfoPhase;
    private final double sampleRate;

    // Parameters
    private volatile boolean enabled = false;
    private volatile double depth = 0.5;    // 0.0 to 1.0
    private volatile double rate = 0.5;     // LFO Speed: 0.1 to 5.0 Hz
    private volatile double feedback = 0.6; // feedback coefficient
    private volatile double wet = 0.0;      // mix

    // Bell filter parameters
    private volatile boolean bellEnabled = true;
    private final BiquadFilter bellFilter;

    public Flanger(double sampleRate) {
        this.sampleRate = sampleRate;
        // 4096 samples at 44.1kHz is approx 92ms, plenty for flanger
        this.delayBuffer = new double[4096];
        this.writeIndex = 0;
        this.lfoPhase = 0.0;
        
        // Initialize peaking filter at 1000Hz, Q=1.5, gain=8.0dB
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
            // Keep buffer updated to avoid clicks when turning on
            delayBuffer[writeIndex] = input;
            writeIndex = (writeIndex + 1) % delayBuffer.length;
            return input;
        }

        // Increment LFO Phase
        lfoPhase += rate / sampleRate;
        if (lfoPhase >= 1.0) {
            lfoPhase -= 1.0;
        }

        // Calculate modulated delay in samples
        // Sine wave LFO: ranges from -1.0 to 1.0
        double lfoVal = Math.sin(2.0 * Math.PI * lfoPhase);
        
        // Delay time sweeps between 1ms and 9ms
        double delayMs = 5.0 + 4.0 * depth * lfoVal;
        double delaySamples = (delayMs * sampleRate) / 1000.0;

        // Read from delay line with linear interpolation
        double readIndex = writeIndex - delaySamples;
        if (readIndex < 0) {
            readIndex += delayBuffer.length;
        }
        
        int idx0 = (int) readIndex;
        int idx1 = (idx0 + 1) % delayBuffer.length;
        double frac = readIndex - idx0;
        
        double delayedSample = delayBuffer[idx0] * (1.0 - frac) + delayBuffer[idx1] * frac;

        // Write back to buffer with feedback
        double feedforward = input + delayedSample * feedback;
        
        // Hard limiter to avoid feedback explosions
        feedforward = Math.max(-1.0, Math.min(1.0, feedforward));
        
        delayBuffer[writeIndex] = feedforward;
        writeIndex = (writeIndex + 1) % delayBuffer.length;

        // Mix dry and wet
        double wetSignal = delayedSample;
        if (bellEnabled) {
            // Sweep bell frequency in sync with the LFO!
            // Frequency sweeps from 400Hz to 3200Hz
            double bellFreq = 400.0 * Math.pow(8.0, (lfoVal + 1.0) / 2.0); 
            bellFilter.setFrequency(bellFreq);
            wetSignal = bellFilter.process(wetSignal);
        }

        return input * (1.0 - wet) + wetSignal * wet;
    }
}
