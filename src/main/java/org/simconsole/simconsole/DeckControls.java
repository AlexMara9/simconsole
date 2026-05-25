package org.simconsole.simconsole;

public class DeckControls {
    
    private volatile double volume = 0.2;
    private volatile double crossfaderGain = 1.0; // Moltiplicatore assegnato dal crossfader
    private volatile double pan = 0.0; // 1.0 = tutto a sinistra, -1.0 = tutto a destra
    private volatile double pitch = 1.0; // Moltiplicatore di velocità (1.0 = originale)
    
    // Funzionalità Key Lock (Master Tempo)
    private volatile boolean keyLock = false;

    // Parametri EQ in Decibel (-12.0 a 12.0)
    private volatile double eqLowDb = 0.0;
    private volatile double eqMidDb = 0.0;
    private volatile double eqHighDb = 0.0;

    private static final double MAX_EQ_GAIN_DB = 12.0;

    public void setPitch(double pitch) {
        this.pitch = Math.max(0.1, Math.min(3.0, pitch)); // Range 10% - 300%
    }

    public double getPitch() {
        return pitch;
    }

    public void setKeyLock(boolean keyLock) {
        this.keyLock = keyLock;
        System.out.println("Key Lock " + (keyLock ? "ATTIVATO" : "DISATTIVATO"));
    }

    public boolean isKeyLock() {
        return keyLock;
    }

    public void toggleKeyLock() {
        setKeyLock(!this.keyLock);
    }

    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
    }

    public void setCrossfaderGain(double gain) {
        this.crossfaderGain = Math.max(0.0, Math.min(1.0, gain));
    }

    public double getVolume() {
        return volume * crossfaderGain;
    }

    public void setPan(double value) {
        this.pan = Math.max(-1.0, Math.min(1.0, value));
    }

    public double getPan() {
        return pan;
    }

    public void setEqLow(double gainDb) {
        this.eqLowDb = Math.max(-MAX_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
    }

    public double getEqLow() {
        return eqLowDb;
    }

    public void setEqMid(double gainDb) {
        this.eqMidDb = Math.max(-MAX_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
    }

    public double getEqMid() {
        return eqMidDb;
    }

    public void setEqHigh(double gainDb) {
        this.eqHighDb = Math.max(-MAX_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
    }

    public double getEqHigh() {
        return eqHighDb;
    }
}
