package org.simconsole.simconsole.components;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;

/**
 * Rotary knob control featuring LED dot indicators around its perimeter.
 */
public class LedKnob extends KnobBase {
    private final BooleanProperty ledsEnabled = new SimpleBooleanProperty(true);
    public BooleanProperty ledsEnabledProperty() { return ledsEnabled; }
    public boolean isLedsEnabled() { return ledsEnabled.get(); }
    public void setLedsEnabled(boolean val) { ledsEnabled.set(val); }
    public LedKnob() {
        super();
        this.getStyleClass().setAll("dynamic-dot-knob");
    }
    @Override
    protected Skin<?> createDefaultSkin() {
        return new LedKnobSkin(this);
    }
}
