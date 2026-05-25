package org.simconsole.simconsole;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class DynamicDeck extends Control {

    // Proprietà
    private final BooleanProperty spinning = new SimpleBooleanProperty(this, "spinning", false);
    private final DoubleProperty spinSpeed = new SimpleDoubleProperty(this, "spinSpeed", 1.0); // gradi per frame
    private final DoubleProperty rotationAngle = new SimpleDoubleProperty(this, "rotationAngle", 0.0);

    private AnimationTimer spinTimer;

    public DynamicDeck() {
        getStyleClass().add("dynamic-deck");

        // Motore di animazione per il giro automatico
        spinTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                setRotationAngle(getRotationAngle() + getSpinSpeed());
            }
        };

        // Ascoltatore che fa partire o fermare il motore
        spinning.addListener((obs, old, isSpinning) -> {
            if (isSpinning) {
                spinTimer.start();
            } else {
                spinTimer.stop();
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CDJDeckSkin(this);
    }

    // --- Property Getters/Setters ---

    public final BooleanProperty spinningProperty() {
        return spinning;
    }
    public final boolean isSpinning() {
        return spinning.get();
    }
    public final void setSpinning(boolean value) {
        spinning.set(value);
    }

    public final DoubleProperty spinSpeedProperty() {
        return spinSpeed;
    }
    public final double getSpinSpeed() {
        return spinSpeed.get();
    }
    public final void setSpinSpeed(double value) {
        spinSpeed.set(value);
    }

    public final DoubleProperty rotationAngleProperty() {
        return rotationAngle;
    }
    public final double getRotationAngle() {
        return rotationAngle.get();
    }
    public final void setRotationAngle(double value) {
        // Non normalizziamo l'angolo (es. % 360) per permettere ai listener 
        // di leggere delta fluidi e continui senza "salti" negativi.
        rotationAngle.set(value);
    }
}
