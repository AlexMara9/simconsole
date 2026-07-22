package org.simconsole.simconsole.models;
public class DeckControls {
    private volatile double volume = 0.2;
    private volatile double crossfaderGain = 1.0;
    private volatile double pan = 0.0;
    private volatile double pitch = 1.0;
    private volatile boolean keyLock = false;
    private volatile double eqLowDb = 0.0;
    private volatile double eqMidDb = 0.0;
    private volatile double eqHighDb = 0.0;
    private static final double MAX_EQ_GAIN_DB = 12.0;
    private static final double MIN_EQ_GAIN_DB = -26.0;
    public void setPitch(double pitch) {
        this.pitch = Math.max(0.5, Math.min(2.0, pitch));
    }
    private volatile boolean flangerEnabled = false;
    private volatile double flangerWet = 0.5;
    private volatile boolean flangerBellEnabled = true;
    private volatile boolean reverbEnabled = false;
    private volatile double reverbWet = 0.5;
    public void setFlangerEnabled(boolean enabled) { this.flangerEnabled = enabled; }
    public boolean isFlangerEnabled() { return this.flangerEnabled; }
    public void setFlangerWet(double wet) { this.flangerWet = wet; }
    public double getFlangerWet() { return this.flangerWet; }
    public void setFlangerBellEnabled(boolean enabled) { this.flangerBellEnabled = enabled; }
    public boolean isFlangerBellEnabled() { return this.flangerBellEnabled; }
    public void setReverbEnabled(boolean enabled) { this.reverbEnabled = enabled; }
    public boolean isReverbEnabled() { return this.reverbEnabled; }
    public void setReverbWet(double wet) { this.reverbWet = wet; }
    public double getReverbWet() { return this.reverbWet; }
    private volatile boolean ecoEnabled = false;
    private volatile double ecoWet = 0.5;
    public void setEcoEnabled(boolean enabled) { this.ecoEnabled = enabled; }
    public boolean isEcoEnabled() { return this.ecoEnabled; }
    public void setEcoWet(double wet) { this.ecoWet = wet; }
    public double getEcoWet() { return this.ecoWet; }
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
        this.eqLowDb = Math.max(MIN_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
    }
    public double getEqLow() {
        return eqLowDb;
    }
    public void setEqMid(double gainDb) {
        this.eqMidDb = Math.max(MIN_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
    }
    public double getEqMid() {
        return eqMidDb;
    }
    public void setEqHigh(double gainDb) {
        this.eqHighDb = Math.max(MIN_EQ_GAIN_DB, Math.min(MAX_EQ_GAIN_DB, gainDb));
    }
    public double getEqHigh() {
        return eqHighDb;
    }
}
