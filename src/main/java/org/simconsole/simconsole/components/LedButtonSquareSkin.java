package org.simconsole.simconsole.components;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
/**
 * Square Skin for {@link LedButton}.
 */
public class LedButtonSquareSkin extends SkinBase<LedButton> {
    private final Rectangle outerRing = new Rectangle();
    private final Rectangle middleRing = new Rectangle();
    private final Rectangle outerKnob = new Rectangle();
    private final Rectangle ledRing = new Rectangle();
    private final Text textNode = new Text();
    private final StackPane container = new StackPane();
    public LedButtonSquareSkin(LedButton control) {
        super(control);
        container.setMinSize(0, 0);
        outerRing.getStyleClass().add("round-button-outer-ring");
        middleRing.getStyleClass().add("round-button-middle-ring");
        outerKnob.getStyleClass().add("round-button-outer-knob");
        ledRing.getStyleClass().add("round-button-led-ring");
        textNode.getStyleClass().add("round-button-text");
        textNode.setBoundsType(javafx.scene.text.TextBoundsType.VISUAL);
        textNode.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        textNode.textProperty().bind(control.textProperty());
        container.getChildren().addAll(outerRing, middleRing, outerKnob, ledRing, textNode);
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
        bindRect(outerRing, minDim, 0.90, 0.20);
        bindRect(middleRing, minDim, 0.86, 0.18);
        bindRect(outerKnob, minDim, 0.82, 0.16);
        bindRect(ledRing, minDim, 0.69, 0.13);
        ledRing.strokeWidthProperty().bind(minDim.multiply(0.06));
        container.layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            updateContentSize(bounds.getWidth(), bounds.getHeight(), control.getTextSizeRatio());
        });
        control.textSizeRatioProperty().addListener((obs, oldRatio, newRatio) -> {
            updateContentSize(container.getWidth(), container.getHeight(), newRatio.doubleValue());
        });
    }
    private void bindRect(Rectangle rect, NumberBinding minDim, double sizeFactor, double arcFactor) {
        rect.widthProperty().bind(minDim.multiply(sizeFactor));
        rect.heightProperty().bind(minDim.multiply(sizeFactor));
        rect.arcWidthProperty().bind(minDim.multiply(arcFactor));
        rect.arcHeightProperty().bind(minDim.multiply(arcFactor));
    }
    private void updateContentSize(double w, double h, double ratio) {
        double size = Math.min(w, h);
        double contentSize = size * ratio;
        if (contentSize > 0) {
            String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", contentSize);
            textNode.setStyle(fontStyle);
            textNode.applyCss();
            Node graphic = getSkinnable().getGraphic();
            if (graphic instanceof javafx.scene.layout.Region) {
                javafx.scene.layout.Region reg = (javafx.scene.layout.Region) graphic;
                reg.setPrefWidth(contentSize);
                reg.setPrefHeight(contentSize);
                reg.setMinSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
                reg.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);
            }
        }
    }
    private void updateVisualState() {
        LedButton control = getSkinnable();
        boolean shouldBeLit = control.isSelected() || control.isArmed();
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
        if (height != -1) return height;
        return Double.MAX_VALUE;
    }
    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (width != -1) return width;
        return Double.MAX_VALUE;
    }
}
