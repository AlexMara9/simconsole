package org.simconsole.simconsole;

import javafx.scene.control.Skin;

public class LedKnob extends KnobBase {

    public LedKnob() {
        super();
        this.getStyleClass().setAll("dynamic-dot-knob");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new LedKnobSkin(this);
    }
}
