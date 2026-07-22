package org.simconsole.simconsole.components;
import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
/**
 * Custom control for a graphical seek slider displaying an audio waveform.
 * Separates logic from visual representation (GraphicSliderSkin).
 */
public class GraphicSlider extends Control {
    private final StringProperty songName = new SimpleStringProperty("no track");
    private final DoubleProperty currentTime = new SimpleDoubleProperty(0.0);
    private final DoubleProperty totalTime = new SimpleDoubleProperty(180.0);
    private final ObjectProperty<float[]> waveform = new SimpleObjectProperty<>();
    public GraphicSlider() {
        this.getStyleClass().add("graphic-slider");
    }
    @Override
    protected Skin<?> createDefaultSkin() {
        return new GraphicSliderSkin(this);
    }
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
    public static class CuePoint {
        public final String id;
        public final double position;
        public final javafx.scene.paint.Color color;
        public CuePoint(String id, double position, javafx.scene.paint.Color color) {
            this.id = id;
            this.position = position;
            this.color = color;
        }
    }
    private final javafx.collections.ObservableMap<String, CuePoint> cuePoints = javafx.collections.FXCollections.observableHashMap();
    public void setCue(String id, javafx.scene.paint.Color color) {
        cuePoints.put(id, new CuePoint(id, getCurrentTime(), color));
    }
    public void removeCue(String id) {
        cuePoints.remove(id);
    }
    public void clearAllCues() {
        cuePoints.clear();
    }
    public void jumpToCue(String id) {
        CuePoint cue = cuePoints.get(id);
        if (cue != null) {
            setCurrentTime(cue.position);
        }
    }
    public javafx.collections.ObservableMap<String, CuePoint> getCuePoints() {
        return cuePoints;
    }
}
