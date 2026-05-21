package org.simconsole.simconsole;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;

/**
 * Standard JavaFX implementation of a responsive, dynamically styled button.
 * It separates the control logic from its visual representation, which is handled
 * by {@link StandardDynamicButtonSkin}.
 */
public class StandardDynamicButton extends ButtonBase {

    private final DoubleProperty textSizeRatio = new SimpleDoubleProperty(0.15);
    private final BooleanProperty toggleMode = new SimpleBooleanProperty(false);
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final BooleanProperty roundShape = new SimpleBooleanProperty(false);

    public StandardDynamicButton() {
        super();
        this.getStyleClass().setAll("dynamic-square-button");
        
        // FORZA IL RAPPORTO DI FORMA 1:1 (QUADRATO)
        // Legando la larghezza all'altezza, ci assicuriamo che il rettangolo di ingombro del Controllo
        // sia sempre un quadrato perfetto. Questo elimina lo "spacing" vuoto ai lati quando inserito
        // in un HBox che ne stira l'altezza.
        this.heightProperty().addListener((obs, oldVal, newVal) -> {
            double h = newVal.doubleValue();
            this.setMinWidth(h);
            this.setPrefWidth(h);
            this.setMaxWidth(h);
        });

        // Ascolta il cambio di forma per cambiare la skin e la classe CSS
        this.roundShape.addListener((obs, oldVal, isRound) -> {
            if (isRound) {
                this.getStyleClass().setAll("dynamic-round-button");
                this.setSkin(new StandardDynamicRoundButtonSkin(this));
            } else {
                this.getStyleClass().setAll("dynamic-square-button");
                this.setSkin(new StandardDynamicSquareButtonSkin(this));
            }
        });
    }

    @Override
    protected javafx.scene.control.Skin<?> createDefaultSkin() {
        return roundShape.get() ? new StandardDynamicRoundButtonSkin(this) : new StandardDynamicSquareButtonSkin(this);
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
