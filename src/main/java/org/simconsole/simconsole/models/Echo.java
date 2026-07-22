package org.simconsole.simconsole.models;

public class Echo {
    private final double[] delayBufferL;
    private final double[] delayBufferR;
    private int writeIndex;
    private final double sampleRate;

    // Parameters
    private volatile boolean enabled = false;
    private volatile double wetAmount = 0.0; // 0.0 to 1.0
    private volatile double delayTimeSec = 0.3; // 300ms
    private volatile double feedback = 0.5; 
    
    private int delaySamples;

    public Echo(double sampleRate) {
        this.sampleRate = sampleRate;
        int maxDelaySamples = (int) (sampleRate * 2.0); // max 2 seconds
        delayBufferL = new double[maxDelaySamples];
        delayBufferR = new double[maxDelaySamples];
        writeIndex = 0;
        updateDelaySamples();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setWet(double wet) {
        this.wetAmount = Math.max(0.0, Math.min(1.0, wet));
    }

    public void setDelayTime(double delaySec) {
        this.delayTimeSec = Math.max(0.01, Math.min(2.0, delaySec));
        updateDelaySamples();
    }

    public void setFeedback(double fb) {
        this.feedback = Math.max(0.0, Math.min(0.95, fb));
    }

    private void updateDelaySamples() {
        delaySamples = (int) (delayTimeSec * sampleRate);
    }

    public double[] process(double inL, double inR) {
        if (!enabled && wetAmount == 0) return new double[]{inL, inR};

        int readIndex = writeIndex - delaySamples;
        if (readIndex < 0) {
            readIndex += delayBufferL.length;
        }

        double delayOutL = delayBufferL[readIndex];
        double delayOutR = delayBufferR[readIndex];

        // Write back to buffer with feedback
        delayBufferL[writeIndex] = inL + delayOutL * feedback;
        delayBufferR[writeIndex] = inR + delayOutR * feedback;

        writeIndex++;
        if (writeIndex >= delayBufferL.length) {
            writeIndex = 0;
        }

        if (enabled) {
            double outL = inL * (1.0 - wetAmount) + delayOutL * wetAmount;
            double outR = inR * (1.0 - wetAmount) + delayOutR * wetAmount;
            return new double[]{outL, outR};
        }
        return new double[]{inL, inR};
    }
}
