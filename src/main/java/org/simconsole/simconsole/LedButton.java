package org.simconsole.simconsole;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.ButtonBase;

/**
 * Standard JavaFX implementation of a responsive, dynamically styled button.
 * It separates the control logic from its visual representation, which is handled
 * by {@link LedButtonRoundSkin} {@link LedButtonSquareSkin}.
 */
public class LedButton extends ButtonBase {

    private final DoubleProperty textSizeRatio = new SimpleDoubleProperty(0.15);
    private final BooleanProperty toggleMode = new SimpleBooleanProperty(false);
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final BooleanProperty roundShape = new SimpleBooleanProperty(false);
    private final javafx.beans.property.ObjectProperty<javafx.geometry.Orientation> layoutBias = 
        new javafx.beans.property.SimpleObjectProperty<>(javafx.geometry.Orientation.HORIZONTAL);

    public LedButton() {
        super();
        this.getStyleClass().setAll("dynamic-square-button");

        // Ascolta il cambio di forma per cambiare la skin e la classe CSS
        this.roundShape.addListener((obs, oldVal, isRound) -> {
            if (isRound) {
                this.getStyleClass().setAll("dynamic-round-button");
                this.setSkin(new LedButtonRoundSkin(this));
            } else {
                this.getStyleClass().setAll("dynamic-square-button");
                this.setSkin(new LedButtonSquareSkin(this));
            }
        });
    }

    public final javafx.beans.property.ObjectProperty<javafx.geometry.Orientation> layoutBiasProperty() { return layoutBias; }
    public final javafx.geometry.Orientation getLayoutBias() { return layoutBias.get(); }
    public final void setLayoutBias(javafx.geometry.Orientation value) { layoutBias.set(value); }

    @Override
    public javafx.geometry.Orientation getContentBias() {
        return getLayoutBias();
    }

    @Override
    protected javafx.scene.control.Skin<?> createDefaultSkin() {
        return roundShape.get() ? new LedButtonRoundSkin(this) : new LedButtonSquareSkin(this);
    }

    @Override
    public void fire() {
        if (!isDisabled()) {
            if (getToggleMode()) {
                setSelected(!isSelected());
            }
            fireEvent(new javafx.event.ActionEvent());
        }
    }

    // --- Properties ---

    public DoubleProperty textSizeRatioProperty() {
        return textSizeRatio;
    }

    public double getTextSizeRatio() {
        return textSizeRatio.get();
    }

    public void setTextSizeRatio(double val) {
        textSizeRatio.set(val);
    }

    public BooleanProperty toggleModeProperty() {
        return toggleMode;
    }

    public boolean getToggleMode() {
        return toggleMode.get();
    }

    public void setToggleMode(boolean val) {
        toggleMode.set(val);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean val) {
        selected.set(val);
    }

    public BooleanProperty roundShapeProperty() {
        return roundShape;
    }

    public boolean getRoundShape() {
        return roundShape.get();
    }

    public void setRoundShape(boolean val) {
        roundShape.set(val);
    }
}
