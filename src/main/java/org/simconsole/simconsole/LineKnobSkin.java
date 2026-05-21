package org.simconsole.simconsole;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

public class LineKnobSkin extends SkinBase<LineKnob> {

    private final Circle outerRing = new Circle();
    private final Circle middleRing = new Circle();
    private final Circle innerKnob = new Circle();
    private final Pane indicatorPane = new Pane();
    private final Line indicator = new Line();
    private final Pane ticksPane = new Pane();
    private final Text leftText = new Text();
    private final Text rightText = new Text();
    private final Line[] ticks = new Line[5];

    private final StackPane container = new StackPane();

    private double startY;
    private double startValue;

    public LineKnobSkin(LineKnob control) {
        super(control);

        // Sblocca il ridimensionamento verso il basso
        container.setMinSize(0, 0);

        outerRing.getStyleClass().add("knob-outer-ring");
        middleRing.getStyleClass().add("knob-middle-ring");
        innerKnob.getStyleClass().add("knob-inner-knob");
        indicator.getStyleClass().add("knob-indicator-line");
        leftText.getStyleClass().add("knob-text");
        rightText.getStyleClass().add("knob-text");

        leftText.textProperty().bind(control.leftLabelProperty());
        rightText.textProperty().bind(control.rightLabelProperty());

        for (int i = 0; i < 5; i++) {
            ticks[i] = new Line();
            ticks[i].getStyleClass().add("knob-tick");
            ticksPane.getChildren().add(ticks[i]);
        }
        ticksPane.getChildren().addAll(leftText, rightText);

        indicatorPane.getChildren().add(indicator);
        indicatorPane.setPickOnBounds(false);
        ticksPane.setPickOnBounds(false);

        container.getChildren().addAll(ticksPane, outerRing, middleRing, innerKnob, indicatorPane);
        getChildren().add(container);

        setupResponsiveBindings(control);
        initInteractivity(control);
    }

    private void setupResponsiveBindings(LineKnob control) {
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
    }

    private void updateLayout(double w, double h) {
        double size = Math.min(w, h);
        double cx = w / 2;
        double cy = h / 2;

        double fontSize = size * 0.08;
        if (fontSize > 0) {
            String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", fontSize);
            leftText.setStyle(fontStyle);
            rightText.setStyle(fontStyle);
            leftText.applyCss();
            rightText.applyCss();
        }

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

        updateIndicator(getSkinnable());
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

    private void updateIndicator(LineKnob control) {
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
    }

    private void initInteractivity(LineKnob control) {
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
        return Double.MAX_VALUE;
    }
}
