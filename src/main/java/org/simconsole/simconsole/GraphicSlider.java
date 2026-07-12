package org.simconsole.simconsole;

import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Custom control for a graphical seek slider displaying an audio waveform.
 * Separates logic from visual representation (GraphicSliderSkin).
 */
public class GraphicSlider extends Control {

    private final StringProperty songName = new SimpleStringProperty("No Track Loaded");
    private final DoubleProperty currentTime = new SimpleDoubleProperty(0.0);
    private final DoubleProperty totalTime = new SimpleDoubleProperty(180.0); // Default 3 minutes
    private final ObjectProperty<float[]> waveform = new SimpleObjectProperty<>();

    public GraphicSlider() {
        this.getStyleClass().add("graphic-slider");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GraphicSliderSkin(this);
    }

    // --- Properties ---

    public StringProperty songNameProperty() {
        return songName;
    }

    public String getSongName() {
        return songName.get();
    }

    public void setSongName(String value) {
        this.songName.set(value);
    }

    public DoubleProperty currentTimeProperty() {
        return currentTime;
    }

    public double getCurrentTime() {
        return currentTime.get();
    }

    public void setCurrentTime(double value) {
        this.currentTime.set(value);
    }

    public DoubleProperty totalTimeProperty() {
        return totalTime;
    }

    public double getTotalTime() {
        return totalTime.get();
    }

    public void setTotalTime(double value) {
        this.totalTime.set(value);
    }

    public ObjectProperty<float[]> waveformProperty() {
        return waveform;
    }

    public float[] getWaveform() {
        return waveform.get();
    }

    public void setWaveform(float[] value) {
        this.waveform.set(value);
    }
}
