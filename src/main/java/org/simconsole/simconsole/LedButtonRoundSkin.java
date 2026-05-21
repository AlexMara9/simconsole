package org.simconsole.simconsole;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

/**
 * Round Skin for {@link LedButton}.
 */
public class LedButtonRoundSkin extends SkinBase<LedButton> {

    private final Circle outerRing = new Circle();
    private final Circle middleRing = new Circle();
    private final Circle outerKnob = new Circle();
    private final Circle ledRing = new Circle();
    private final Circle innerKnob = new Circle();
    private final Text textNode = new Text();

    private final StackPane container = new StackPane();

    public LedButtonRoundSkin(LedButton control) {
        super(control);

        // Sblocca il ridimensionamento verso il basso
        container.setMinSize(0, 0);

        outerRing.getStyleClass().add("round-button-outer-ring");
        middleRing.getStyleClass().add("round-button-middle-ring");
        outerKnob.getStyleClass().add("round-button-outer-knob");
        ledRing.getStyleClass().add("round-button-led-ring");
        innerKnob.getStyleClass().add("round-button-inner-knob");
        textNode.getStyleClass().add("round-button-text");

        textNode.textProperty().bind(control.textProperty());

        container.getChildren().addAll(outerRing, middleRing, outerKnob, ledRing, innerKnob, textNode);
        getChildren().add(container);

        control.graphicProperty().addListener((obs, oldNode, newNode) -> updateGraphic(oldNode, newNode));
        updateGraphic(null, control.getGraphic());

        setupResponsiveBindings(control);

        control.armedProperty().addListener((obs, old, armed) -> updateVisualState());
        control.selectedProperty().addListener((obs, old, selected) -> updateVisualState());

        container.setOnMousePressed(e -> control.arm());
        
        container.setOnMouseReleased(e -> {
            boolean wasArmed = control.isArmed();
            control.disarm();
            if (wasArmed && container.getLayoutBounds().contains(e.getX(), e.getY())) {
                control.fire();
            }
        });

        container.setOnMouseExited(e -> {
            if (control.isArmed()) control.disarm();
        });

        container.setOnMouseEntered(e -> {
            if (e.isPrimaryButtonDown()) control.arm();
        });
    }

    private void updateGraphic(Node oldNode, Node newNode) {
        if (oldNode != null) {
            container.getChildren().remove(oldNode);
        }
        if (newNode != null) {
            container.getChildren().add(newNode);
        }
    }

    private void setupResponsiveBindings(LedButton control) {
        NumberBinding minDim = Bindings.min(container.widthProperty(), container.heightProperty());

        outerRing.radiusProperty().bind(minDim.multiply(0.45));
        middleRing.radiusProperty().bind(minDim.multiply(0.43));
        outerKnob.radiusProperty().bind(minDim.multiply(0.41));
        ledRing.radiusProperty().bind(minDim.multiply(0.36));
        innerKnob.radiusProperty().bind(minDim.multiply(0.33));

        container.layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            updateContentSize(bounds.getWidth(), bounds.getHeight(), control.getTextSizeRatio());
        });
        control.textSizeRatioProperty().addListener((obs, oldRatio, newRatio) -> {
            updateContentSize(container.getWidth(), container.getHeight(), newRatio.doubleValue());
        });
    }

    private void updateContentSize(double w, double h, double ratio) {
        double size = Math.min(w, h);
        double contentSize = size * ratio;
        if (contentSize > 0) {
            String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", contentSize);
            textNode.setStyle(fontStyle);
            textNode.applyCss();
            
            // Applica la ratio anche alla grafica (SVG Region, etc.)
            Node graphic = getSkinnable().getGraphic();
            if (graphic instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region reg = (javafx.scene.layout.Region) graphic;
                reg.setPrefWidth(contentSize);
                reg.setPrefHeight(contentSize);
            }
        }
    }

    private void updateVisualState() {
        LedButton control = getSkinnable();
        boolean shouldBeLit = control.getToggleMode() ? (control.isSelected() || control.isArmed()) : control.isArmed();

        if (shouldBeLit) {
            if (!ledRing.getStyleClass().contains("pressed")) {
                ledRing.getStyleClass().add("pressed");
            }
        } else {
            ledRing.getStyleClass().remove("pressed");
        }
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
        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return Double.MAX_VALUE;
    }
}
