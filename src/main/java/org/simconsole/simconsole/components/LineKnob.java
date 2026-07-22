package org.simconsole.simconsole.components;
import javafx.scene.control.Skin;

/**
 * Rotary knob control featuring a traditional line indicator and tick marks.
 */
public class LineKnob extends KnobBase {
    public LineKnob() {
        super();
        this.getStyleClass().setAll("dynamic-knob");
    }
    @Override
    protected Skin<?> createDefaultSkin() {
        return new LineKnobSkin(this);
    }
}
