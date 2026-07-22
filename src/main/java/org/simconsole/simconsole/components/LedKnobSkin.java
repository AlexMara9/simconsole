package org.simconsole.simconsole.components;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class LedKnobSkin extends SkinBase<LedKnob> {

    private final Circle outerRing = new Circle();
    private final Circle middleRing = new Circle();
    private final Circle innerKnob = new Circle();
    private final Pane indicatorPane = new Pane();
    private final Circle indicator = new Circle();
    private final Pane dotsPane = new Pane();
    private final Text leftText = new Text();
    private final Text rightText = new Text();

    private final Circle[] dots = new Circle[11];

    private final StackPane container = new StackPane();

    private double startY;
    private double startValue;

    public LedKnobSkin(LedKnob control) {
        super(control);

        // Sblocca il ridimensionamento verso il basso
        container.setMinSize(0, 0);

        outerRing.getStyleClass().add("knob-outer-ring");
        middleRing.getStyleClass().add("knob-middle-ring");
        innerKnob.getStyleClass().add("knob-inner-knob");
        indicator.getStyleClass().add("knob-indicator-dot");

        leftText.getStyleClass().add("knob-text");
        rightText.getStyleClass().add("knob-text");
        leftText.textProperty().bind(control.leftLabelProperty());
        rightText.textProperty().bind(control.rightLabelProperty());

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new Circle();
            dots[i].getStyleClass().add("knob-dot");
            dotsPane.getChildren().add(dots[i]);
        }

        indicatorPane.getChildren().add(indicator);
        indicatorPane.setPickOnBounds(false);
        dotsPane.getChildren().addAll(leftText, rightText);
        dotsPane.setPickOnBounds(false);

        container.getChildren().addAll(dotsPane, outerRing, middleRing, innerKnob, indicatorPane);
        getChildren().add(container);

        container.setPickOnBounds(false);

        setupResponsiveBindings(control);
        initInteractivity(control);
    }

    private void setupResponsiveBindings(LedKnob control) {
        NumberBinding minDim = Bindings.min(container.widthProperty(), container.heightProperty());

        outerRing.radiusProperty().bind(minDim.multiply(0.28));
        middleRing.radiusProperty().bind(minDim.multiply(0.26));
        innerKnob.radiusProperty().bind(minDim.multiply(0.24));

        container.layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            updateLayout(bounds.getWidth(), bounds.getHeight());
        });

        control.valueProperty().addListener((obs, old, val) -> updateIndicator(control));
        control.minProperty().addListener((obs, old, val) -> updateIndicator(control));
        control.maxProperty().addListener((obs, old, val) -> updateIndicator(control));
        control.ledsEnabledProperty().addListener((obs, old, val) -> updateIndicator(control));
    }

    private void updateLayout(double w, double h) {
        double size = Math.min(w, h);
        double cx = w / 2;
        double cy = h / 2;

        double fontSize = size * 0.115;
        if (fontSize > 0) {
            String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", fontSize);
            leftText.setStyle(fontStyle);
            rightText.setStyle(fontStyle);
            leftText.applyCss();
            rightText.applyCss();
        }

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

        updateIndicator(getSkinnable());
    }

    private void positionText(Text text, double cx, double cy, double radius, double angleDeg) {
        double angleRad = Math.toRadians(angleDeg - 90);
        double tw = text.getLayoutBounds().getWidth();
        double th = text.getLayoutBounds().getHeight();
        text.setLayoutX(cx + radius * Math.cos(angleRad) - tw / 2);
        double add = (cy + radius * Math.sin(angleRad) + th / 4) * 0.1;
        text.setLayoutY(cy + radius * Math.sin(angleRad) + th / 4 + add);
    }

    private void updateIndicator(LedKnob control) {
        if (control == null) return;
        double val = control.getValue();
        double minVal = control.getMin();
        double maxVal = control.getMax();

        // Prevent division by zero
        if (maxVal == minVal) return;

        double percentage = (val - minVal) / (maxVal - minVal);
        percentage = Math.max(0, Math.min(1, percentage));

        double angle = -135 + percentage * 270;
        indicatorPane.setRotate(angle);

        int activeDots = (int) Math.round(percentage * (dots.length - 1));
        if (!control.isLedsEnabled()) activeDots = -1; // Turn off all LED dots

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

    private void initInteractivity(LedKnob control) {
        container.setOnMousePressed(e -> {
            startY = e.getSceneY();
            startValue = control.getValue();
        });
        container.setOnMouseDragged(e -> {
            double deltaY = startY - e.getSceneY();
            double deltaVal = (deltaY / 150.0) * (control.getMax() - control.getMin());
            double newVal = startValue + deltaVal;
            newVal = Math.max(control.getMin(), Math.min(control.getMax(), newVal));
            control.setValue(newVal);
        });
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        layoutInArea(container, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + rightInset + 30; 
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + bottomInset + 30; 
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (height != -1) return height; 
        return leftInset + rightInset + 50; 
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (width != -1) return width;
        return topInset + bottomInset + 50;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (height != -1) return height;
        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (width != -1) return width;
        return Double.MAX_VALUE;
    }
}
