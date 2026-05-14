package org.simconsole.simconsole;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public class DynamicDotKnob extends StackPane {
    private final DoubleProperty value = new SimpleDoubleProperty(0.0);
    private final DoubleProperty min = new SimpleDoubleProperty(0.0);
    private final DoubleProperty max = new SimpleDoubleProperty(1.0);

    private final javafx.beans.property.StringProperty leftLabel = new javafx.beans.property.SimpleStringProperty("Min");
    private final javafx.beans.property.StringProperty rightLabel = new javafx.beans.property.SimpleStringProperty("Max");

    private final Circle outerRing = new Circle();
    private final Circle middleRing = new Circle();
    private final Circle innerKnob = new Circle();
    private final Pane indicatorPane = new Pane();
    private final Circle indicator = new Circle();
    private final Pane dotsPane = new Pane();
    private final javafx.scene.text.Text leftText = new javafx.scene.text.Text();
    private final javafx.scene.text.Text rightText = new javafx.scene.text.Text();

    private final Circle[] dots = new Circle[11];

    public DynamicDotKnob() {
        this.getStyleClass().add("dynamic-dot-knob");
        
        outerRing.getStyleClass().add("knob-outer-ring");
        middleRing.getStyleClass().add("knob-middle-ring");
        innerKnob.getStyleClass().add("knob-inner-knob");
        indicator.getStyleClass().add("knob-indicator-dot");
        
        leftText.getStyleClass().add("knob-text");
        rightText.getStyleClass().add("knob-text");
        leftText.textProperty().bind(leftLabel);
        rightText.textProperty().bind(rightLabel);

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new Circle();
            dots[i].getStyleClass().add("knob-dot");
            dotsPane.getChildren().add(dots[i]);
        }
        
        indicatorPane.getChildren().add(indicator);
        indicatorPane.setPickOnBounds(false);
        dotsPane.getChildren().addAll(leftText, rightText);
        dotsPane.setPickOnBounds(false);
        this.setPickOnBounds(false);

        this.getChildren().addAll(dotsPane, outerRing, middleRing, innerKnob, indicatorPane);

        initResponsive();
        initInteractivity();
    }

    private void initResponsive() {
        outerRing.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.28));
        middleRing.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.26));
        innerKnob.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.24));

        this.layoutBoundsProperty().addListener((obs, old, bounds) -> updateLayout(bounds));
        value.addListener((obs, old, val) -> updateIndicator());
    }

    private void updateLayout(Bounds bounds) {
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        double size = Math.min(w, h);
        double cx = w / 2;
        double cy = h / 2;

        double fontSize = size * 0.15;
        String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", fontSize);
        leftText.setStyle(fontStyle);
        rightText.setStyle(fontStyle);
        leftText.applyCss();
        rightText.applyCss();

        double textRadius = size * 0.45;
        positionText(leftText, cx, cy, textRadius, -135);
        positionText(rightText, cx, cy, textRadius, 135);

        double dotRadius = size * 0.38;
        double startAngle = -135;
        double endAngle = 135;
        double step = (endAngle - startAngle) / (dots.length - 1);
        
        double singleDotRadius = size * 0.018; 

        for (int i = 0; i < dots.length; i++) {
            double angle = startAngle + step * i;
            double angleRad = Math.toRadians(angle - 90);
            dots[i].setCenterX(cx + dotRadius * Math.cos(angleRad));
            dots[i].setCenterY(cy + dotRadius * Math.sin(angleRad));
            dots[i].setRadius(singleDotRadius);
        }

        indicator.setRadius(singleDotRadius * 1.5);
        indicator.setCenterX(cx);
        indicator.setCenterY(cy - size * 0.18); 
        
        updateIndicator();
    }

    private void positionText(javafx.scene.text.Text text, double cx, double cy, double radius, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg - 90);
        double tw = text.getLayoutBounds().getWidth();
        double th = text.getLayoutBounds().getHeight();
//        radius+=3;
        text.setLayoutX(cx + radius * Math.cos(angleRad) - tw / 2);
        double add = (cy + radius * Math.sin(angleRad) + th / 4)*0.1;
        text.setLayoutY(cy + radius * Math.sin(angleRad) + th / 4 + add);
    }

    private void updateIndicator() {
        double val = value.get();
        double minVal = min.get();
        double maxVal = max.get();
        double percentage = (val - minVal) / (maxVal - minVal);
        percentage = Math.max(0, Math.min(1, percentage));
        
        double angle = -135 + percentage * 270;
        indicatorPane.setRotate(angle);

        int activeDots = (int) Math.round(percentage * (dots.length - 1));
        for (int i = 0; i < dots.length; i++) {
            if (i <= activeDots) {
                if (!dots[i].getStyleClass().contains("knob-dot-active")) {
                    dots[i].getStyleClass().add("knob-dot-active");
                }
            } else {
                dots[i].getStyleClass().remove("knob-dot-active");
            }
        }
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
