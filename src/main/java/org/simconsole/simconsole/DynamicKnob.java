package org.simconsole.simconsole;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class DynamicKnob extends StackPane {
    private final DoubleProperty value = new SimpleDoubleProperty(0.5);
    private final DoubleProperty min = new SimpleDoubleProperty(0.0);
    private final DoubleProperty max = new SimpleDoubleProperty(1.0);

    private final javafx.beans.property.StringProperty leftLabel = new javafx.beans.property.SimpleStringProperty("L");
    private final javafx.beans.property.StringProperty rightLabel = new javafx.beans.property.SimpleStringProperty("R");

    private final Circle outerRing = new Circle();
    private final Circle middleRing = new Circle();
    private final Circle innerKnob = new Circle();
    private final Pane indicatorPane = new Pane();
    private final Line indicator = new Line();
    private final Pane ticksPane = new Pane();

    private final Text leftText = new Text();
    private final Text rightText = new Text();
    private final Line[] ticks = new Line[5];

    public DynamicKnob() {
        this.getStyleClass().add("dynamic-knob");
        
        outerRing.getStyleClass().add("knob-outer-ring");
        middleRing.getStyleClass().add("knob-middle-ring");
        innerKnob.getStyleClass().add("knob-inner-knob");
        indicator.getStyleClass().add("knob-indicator-line");
        leftText.getStyleClass().add("knob-text");
        rightText.getStyleClass().add("knob-text");
        
        leftText.textProperty().bind(leftLabel);
        rightText.textProperty().bind(rightLabel);

        for (int i = 0; i < 5; i++) {
            ticks[i] = new Line();
            ticks[i].getStyleClass().add("knob-tick");
            ticksPane.getChildren().add(ticks[i]);
        }
        ticksPane.getChildren().addAll(leftText, rightText);
        
        indicatorPane.getChildren().add(indicator);
        indicatorPane.setPickOnBounds(false);
        ticksPane.setPickOnBounds(false);
        this.setPickOnBounds(false);

        this.getChildren().addAll(ticksPane, outerRing, middleRing, innerKnob, indicatorPane);

        initResponsive();
        initInteractivity();
    }

    private void initResponsive() {
        outerRing.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.28));
        middleRing.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.26));
        innerKnob.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.24));

        this.layoutBoundsProperty().addListener((obs, old, bounds) -> {
            updateLayout(bounds);
        });

        value.addListener((obs, old, val) -> updateIndicator());
    }

    private void updateLayout(Bounds bounds) {
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        double size = Math.min(w, h);
        double cx = w / 2;
        double cy = h / 2;

        double fontSize = size * 0.08;
        String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", fontSize);
        leftText.setStyle(fontStyle);
        rightText.setStyle(fontStyle);
        leftText.applyCss();
        rightText.applyCss();

        double textRadius = size * 0.40;
        
        positionText(leftText, cx, cy, textRadius, -135);
        positionText(rightText, cx, cy, textRadius, 135);

        double tickRadiusInner = size * 0.32;
        double tickRadiusOuter = size * 0.36;
        double startAngle = -100;
        double endAngle = 100;
        double step = (endAngle - startAngle) / 4;
        for (int i = 0; i < 5; i++) {
            double angle = startAngle + step * i;
            positionLine(ticks[i], cx, cy, tickRadiusInner, tickRadiusOuter, angle);
        }

        double indicatorR1 = size * 0.12;
        double indicatorR2 = size * 0.22;
        indicator.setStartX(cx);
        indicator.setStartY(cy - indicatorR1);
        indicator.setEndX(cx);
        indicator.setEndY(cy - indicatorR2);
        
        updateIndicator();
    }

    private void positionText(Text text, double cx, double cy, double radius, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg - 90);
        double tw = text.getLayoutBounds().getWidth();
        double th = text.getLayoutBounds().getHeight();
        text.setLayoutX(cx + radius * Math.cos(angleRad) - tw / 2);
        text.setLayoutY(cy + radius * Math.sin(angleRad) + th / 4);
    }

    private void positionLine(Line line, double cx, double cy, double rInner, double rOuter, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg - 90);
        line.setStartX(cx + rInner * Math.cos(angleRad));
        line.setStartY(cy + rInner * Math.sin(angleRad));
        line.setEndX(cx + rOuter * Math.cos(angleRad));
        line.setEndY(cy + rOuter * Math.sin(angleRad));
    }

    private void updateIndicator() {
        double val = value.get();
        double minVal = min.get();
        double maxVal = max.get();
        double percentage = (val - minVal) / (maxVal - minVal);
        percentage = Math.max(0, Math.min(1, percentage));
        double angle = -135 + percentage * 270; 
        indicatorPane.setRotate(angle);
    }

    private double startY;
    private double startValue;

    private void initInteractivity() {
        this.setOnMousePressed(e -> {
            startY = e.getSceneY();
            startValue = value.get();
        });
        this.setOnMouseDragged(e -> {
            double deltaY = startY - e.getSceneY();
            double deltaVal = (deltaY / 150.0) * (max.get() - min.get());
            double newVal = startValue + deltaVal;
            newVal = Math.max(min.get(), Math.min(max.get(), newVal));
            value.set(newVal);
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

    public javafx.beans.property.StringProperty leftLabelProperty() { return leftLabel; }
    public String getLeftLabel() { return leftLabel.get(); }
    public void setLeftLabel(String val) { leftLabel.set(val); }

    public javafx.beans.property.StringProperty rightLabelProperty() { return rightLabel; }
    public String getRightLabel() { return rightLabel.get(); }
    public void setRightLabel(String val) { rightLabel.set(val); }
}
