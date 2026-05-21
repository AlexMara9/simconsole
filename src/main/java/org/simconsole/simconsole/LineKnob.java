package org.simconsole.simconsole;

import javafx.scene.control.Skin;

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
