package org.simconsole.simconsole.models;
public class Reverb {
    private final double[] delayL1, delayL2, delayL3, delayL4;
    private final double[] delayR1, delayR2, delayR3, delayR4;
    private int idxL1, idxL2, idxL3, idxL4;
    private int idxR1, idxR2, idxR3, idxR4;
    private double dampL1, dampL2, dampL3, dampL4;
    private double dampR1, dampR2, dampR3, dampR4;
    private volatile boolean enabled = false;
    private volatile double wet = 0.0;
    private volatile double roomSize = 0.84;
    private volatile double damping = 0.5;
    public Reverb(double sampleRate) {
        delayL1 = new double[1117];
        delayL2 = new double[1357];
        delayL3 = new double[1423];
        delayL4 = new double[1621];
        delayR1 = new double[1187];
        delayR2 = new double[1297];
        delayR3 = new double[1481];
        delayR4 = new double[1583];
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public void setWet(double wet) {
        this.wet = Math.max(0.0, Math.min(1.0, wet));
    }
    public void setRoomSize(double roomSize) {
        this.roomSize = Math.max(0.0, Math.min(0.98, roomSize));
    }
    public void setDamping(double damping) {
        this.damping = Math.max(0.0, Math.min(1.0, damping));
    }
    public double[] process(double inputL, double inputR) {
        if (!enabled || wet <= 0.001) {
            return new double[]{inputL, inputR};
        }
        double outL1 = delayL1[idxL1];
        double outL2 = delayL2[idxL2];
        double outL3 = delayL3[idxL3];
        double outL4 = delayL4[idxL4];
        double outR1 = delayR1[idxR1];
        double outR2 = delayR2[idxR2];
        double outR3 = delayR3[idxR3];
        double outR4 = delayR4[idxR4];
        dampL1 = dampL1 * damping + outL1 * (1.0 - damping);
        dampL2 = dampL2 * damping + outL2 * (1.0 - damping);
        dampL3 = dampL3 * damping + outL3 * (1.0 - damping);
        dampL4 = dampL4 * damping + outL4 * (1.0 - damping);
        dampR1 = dampR1 * damping + outR1 * (1.0 - damping);
        dampR2 = dampR2 * damping + outR2 * (1.0 - damping);
        dampR3 = dampR3 * damping + outR3 * (1.0 - damping);
        dampR4 = dampR4 * damping + outR4 * (1.0 - damping);
        double wetL = (dampL1 + dampL2 + dampL3 + dampL4) * 0.25;
        double wetR = (dampR1 + dampR2 + dampR3 + dampR4) * 0.25;
        double sumL = (dampL1 + dampL2 + dampL3 + dampL4) * 0.5;
        double sumR = (dampR1 + dampR2 + dampR3 + dampR4) * 0.5;
        double inL1 = inputL + (dampL1 - sumL) * roomSize;
        double inL2 = inputL + (dampL2 - sumL) * roomSize;
        double inL3 = inputL + (dampL3 - sumL) * roomSize;
        double inL4 = inputL + (dampL4 - sumL) * roomSize;
        double inR1 = inputR + (dampR1 - sumR) * roomSize;
        double inR2 = inputR + (dampR2 - sumR) * roomSize;
        double inR3 = inputR + (dampR3 - sumR) * roomSize;
        double inR4 = inputR + (dampR4 - sumR) * roomSize;
        delayL1[idxL1] = inL1;
        delayL2[idxL2] = inL2;
        delayL3[idxL3] = inL3;
        delayL4[idxL4] = inL4;
        delayR1[idxR1] = inR1;
        delayR2[idxR2] = inR2;
        delayR3[idxR3] = inR3;
        delayR4[idxR4] = inR4;
        idxL1 = (idxL1 + 1) % delayL1.length;
        idxL2 = (idxL2 + 1) % delayL2.length;
        idxL3 = (idxL3 + 1) % delayL3.length;
        idxL4 = (idxL4 + 1) % delayL4.length;
        idxR1 = (idxR1 + 1) % delayR1.length;
        idxR2 = (idxR2 + 1) % delayR2.length;
        idxR3 = (idxR3 + 1) % delayR3.length;
        idxR4 = (idxR4 + 1) % delayR4.length;
        return new double[]{
            inputL * (1.0 - wet) + wetL * wet,
            inputR * (1.0 - wet) + wetR * wet
        };
    }
}
