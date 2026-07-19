package org.simconsole.simconsole;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;

/**
 * A label whose font size scales dynamically based on its container's layout bounds.
 * Uses a standard Control/Skin architecture.
 */
public class ResponsiveLabel extends Label {

    // The ratio of font size relative to the component's width or height
    private final DoubleProperty textSizeRatio = new SimpleDoubleProperty(0.2);

    public ResponsiveLabel() {
        super();
        this.getStyleClass().add("responsive-label");
        // Apply default styles that can be overridden by CSS
        this.setStyle("-fx-font-family: 'Segoe UI', 'Inter', sans-serif;");
    }

    public ResponsiveLabel(String text) {
        this();
        setText(text);
    }

    @Override
    protected javafx.scene.control.Skin<?> createDefaultSkin() {
        return new ResponsiveLabelSkin(this);
    }

    public DoubleProperty textSizeRatioProperty() {
        return textSizeRatio;
    }

    public double getTextSizeRatio() {
        return textSizeRatio.get();
    }

    public void setTextSizeRatio(double val) {
        textSizeRatio.set(val);
    }
}
