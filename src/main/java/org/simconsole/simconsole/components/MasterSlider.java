package org.simconsole.simconsole.components;

import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.control.Skin;

/**
 * Custom Slider implementing a Studio Mixing Console Fader.
 * Uses a thin track with symmetric tick marks and a 3D ribbed thumb.
 */
public class MasterSlider extends Slider {

    public MasterSlider() {
        super();
        this.getStyleClass().add("master-slider");
    }

    public MasterSlider(double min, double max, double value) {
        super(min, max, value);
        this.getStyleClass().add("master-slider");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MasterSliderSkin(this);
    }

    @Override
    public Orientation getContentBias() {
        return getOrientation() == Orientation.VERTICAL ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }
}
