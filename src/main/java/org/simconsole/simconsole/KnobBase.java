package org.simconsole.simconsole;

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

    private final StringProperty leftLabel = new SimpleStringProperty("L");
    private final StringProperty rightLabel = new SimpleStringProperty("R");

    public KnobBase() {
        super();
        
        // FORZA IL RAPPORTO DI FORMA 1:1 (QUADRATO) COME LIMITE MASSIMO E PREFERITO
        // Legando la larghezza preferita e massima all'altezza, ci assicuriamo che la manopola 
        // non diventi mai "larga" con spazi vuoti ai lati. 
        // NON leghiamo la minWidth per permettere all'HBox di schiacciare orizzontalmente in caso di poco spazio.
        this.heightProperty().addListener((obs, oldVal, newVal) -> {
            double h = newVal.doubleValue();
            this.setPrefWidth(h);
            this.setMaxWidth(h);
        });
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
