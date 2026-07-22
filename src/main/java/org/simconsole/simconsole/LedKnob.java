package org.simconsole.simconsole;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;

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
