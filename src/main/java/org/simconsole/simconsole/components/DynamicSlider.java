package org.simconsole.simconsole.components;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.control.Skin;
/**
 * Standard JavaFX implementation of a responsive, dynamically styled Slider.
 * It separates the control logic from its visual representation, which is handled
 * by {@link DynamicSliderSkin}.
 */
public class DynamicSlider extends Slider {
    public DynamicSlider() {
        super();
        this.getStyleClass().add("dynamic-slider");
    }
    public DynamicSlider(double min, double max, double value) {
        super(min, max, value);
        this.getStyleClass().add("dynamic-slider");
    }
    @Override
    protected Skin<?> createDefaultSkin() {
        return new DynamicSliderSkin(this);
    }
    @Override
    public Orientation getContentBias() {
        return getOrientation() == Orientation.VERTICAL ? Orientation.VERTICAL : Orientation.HORIZONTAL;
    }
}
