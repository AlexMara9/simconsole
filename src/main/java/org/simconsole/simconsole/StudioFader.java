package org.simconsole.simconsole;

import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.control.Skin;

/**
 * Custom Slider implementing a Studio Mixing Console Fader.
 * Uses a thin track with symmetric tick marks and a 3D ribbed thumb.
 */
public class StudioFader extends Slider {

    public StudioFader() {
        super();
        this.getStyleClass().add("studio-fader");
    }

    public StudioFader(double min, double max, double value) {
        super(min, max, value);
        this.getStyleClass().add("studio-fader");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new StudioFaderSkin(this);
    }

    @Override
    public Orientation getContentBias() {
        return getOrientation() == Orientation.VERTICAL ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }
}
