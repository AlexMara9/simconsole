package org.simconsole.simconsole.components;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Custom JavaFX control representing an interactive CDJ-style turntable deck.
 */
public class DynamicDeck extends Control {
    private final BooleanProperty scrubbing = new SimpleBooleanProperty(this, "scrubbing", false);
    private final DoubleProperty rotationAngle = new SimpleDoubleProperty(this, "rotationAngle", 0.0);
    public DynamicDeck() {
        getStyleClass().add("dynamic-deck");
    }
    @Override
    protected Skin<?> createDefaultSkin() {
        return new CDJDeckSkin(this);
    }
    public final BooleanProperty scrubbingProperty() {
        return scrubbing;
    }
    public final boolean isScrubbing() {
        return scrubbing.get();
    }
    public final void setScrubbing(boolean value) {
        scrubbing.set(value);
    }
    public final DoubleProperty rotationAngleProperty() {
        return rotationAngle;
    }
    public final double getRotationAngle() {
        return rotationAngle.get();
    }
    public final void setRotationAngle(double value) {
        rotationAngle.set(value);
    }
}
