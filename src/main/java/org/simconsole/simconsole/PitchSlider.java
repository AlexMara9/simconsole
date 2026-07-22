package org.simconsole.simconsole;

import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.control.Skin;

/**
 * Custom Slider implementing a DJ Pitch Fader.
 */
public class PitchSlider extends Slider {

    public PitchSlider() {
        super(0.5, 2.0, 1.0);
        this.getStyleClass().add("pitch-slider");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PitchSliderSkin(this);
    }

    @Override
    public Orientation getContentBias() {
        return getOrientation() == Orientation.VERTICAL ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }
}
