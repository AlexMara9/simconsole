package org.simconsole.simconsole.components;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
/**
 * Classe base astratta per tutte le manopole (Knobs).
 * Contiene la logica di stato (valore, min, max, label) e i vincoli di resize,
 * separandoli completamente dalla rappresentazione grafica (Skin).
 */
public abstract class KnobBase extends Control {
    private final DoubleProperty value = new SimpleDoubleProperty(0.0);
    private final DoubleProperty min = new SimpleDoubleProperty(0.0);
    private final DoubleProperty max = new SimpleDoubleProperty(1.0);
    private final javafx.beans.property.ObjectProperty<javafx.geometry.Orientation> layoutBias =
        new javafx.beans.property.SimpleObjectProperty<>(javafx.geometry.Orientation.HORIZONTAL);
    private final StringProperty leftLabel = new SimpleStringProperty("L");
    private final StringProperty rightLabel = new SimpleStringProperty("R");
    public KnobBase() {
        super();
        this.setPickOnBounds(false);
    }
    public final javafx.beans.property.ObjectProperty<javafx.geometry.Orientation> layoutBiasProperty() { return layoutBias; }
    public final javafx.geometry.Orientation getLayoutBias() { return layoutBias.get(); }
    public final void setLayoutBias(javafx.geometry.Orientation value) { layoutBias.set(value); }
    @Override
    public javafx.geometry.Orientation getContentBias() {
        return getLayoutBias();
    }
    public DoubleProperty valueProperty() { return value; }
    public double getValue() { return value.get(); }
    public void setValue(double val) { value.set(val); }
    public DoubleProperty minProperty() { return min; }
    public double getMin() { return min.get(); }
    public void setMin(double val) { min.set(val); }
    public DoubleProperty maxProperty() { return max; }
    public double getMax() { return max.get(); }
    public void setMax(double val) { max.set(val); }
    public StringProperty leftLabelProperty() { return leftLabel; }
    public String getLeftLabel() { return leftLabel.get(); }
    public void setLeftLabel(String val) { leftLabel.set(val); }
    public StringProperty rightLabelProperty() { return rightLabel; }
    public String getRightLabel() { return rightLabel.get(); }
    public void setRightLabel(String val) { rightLabel.set(val); }
}
