package org.simconsole.simconsole;

import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

@DefaultProperty("graphic")
public class DynamicRoundButton extends StackPane {
    private final StringProperty text = new SimpleStringProperty("");
    private final DoubleProperty textSizeRatio = new SimpleDoubleProperty(0.15);
    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();
    private final BooleanProperty toggleButton = new SimpleBooleanProperty(false);

    private final Circle outerRing = new Circle();
    private final Circle middleRing = new Circle();
    private final Circle outerKnob = new Circle();
    private final Circle ledRing = new Circle();
    private final Circle innerKnob = new Circle();
    private final Text textNode = new Text();
    private final Circle hitBox = new Circle();

    public DynamicRoundButton() {
        this.getStyleClass().add("dynamic-round-button");

        outerRing.getStyleClass().add("round-button-outer-ring");
        middleRing.getStyleClass().add("round-button-middle-ring");
        outerKnob.getStyleClass().add("round-button-outer-knob");
        ledRing.getStyleClass().add("round-button-led-ring");
        innerKnob.getStyleClass().add("round-button-inner-knob");
        textNode.getStyleClass().add("round-button-text");

        textNode.textProperty().bind(text);
        textNode.setPickOnBounds(false);
        this.setPickOnBounds(false);

        hitBox.setFill(javafx.scene.paint.Color.TRANSPARENT);

        this.getChildren().addAll(outerRing, middleRing, outerKnob, ledRing, innerKnob, textNode, hitBox);

        initResponsive();
        initInteractivity();
        initGraphicListener();
    }

    private void initGraphicListener() {
        graphic.addListener((obs, oldNode, newNode) -> {
            if (oldNode != null) {
                getChildren().remove(oldNode);
            }
            if (newNode != null) {
                // Make sure graphic is placed underneath hitBox so click events are not blocked
                int hitBoxIndex = getChildren().indexOf(hitBox);
                if (hitBoxIndex >= 0) {
                    getChildren().add(hitBoxIndex, newNode);
                } else {
                    getChildren().add(newNode);
                }
            }
        });
    }

    private void initResponsive() {
        outerRing.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.45));
        middleRing.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.43));
        outerKnob.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.41));
        ledRing.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.36));
        innerKnob.radiusProperty().bind(javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty()).multiply(0.33));
        
        hitBox.radiusProperty().bind(outerRing.radiusProperty());

        this.layoutBoundsProperty().addListener((obs, old, bounds) -> updateLayout(bounds));
        textSizeRatio.addListener((obs, old, val) -> updateLayout(this.getLayoutBounds()));
    }

    private void updateLayout(Bounds bounds) {
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        double size = Math.min(w, h);

        double fontSize = size * textSizeRatio.get();
        String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", fontSize);
        textNode.setStyle(fontStyle);
        textNode.applyCss();
    }

    private void initInteractivity() {
        hitBox.setOnMousePressed(e -> {
            if (!ledRing.getStyleClass().contains("pressed")) {
                ledRing.getStyleClass().add("pressed");
            }else if(toggleButton.get()){
                ledRing.getStyleClass().remove("pressed");
            }
        });
        hitBox.setOnMouseReleased(e -> {
            if(!toggleButton.get()){
                ledRing.getStyleClass().remove("pressed");
            }
        });
        hitBox.setOnMouseExited(e -> {
            if(!toggleButton.get()){
                ledRing.getStyleClass().remove("pressed");
            }
        });
    }

    public StringProperty textProperty() { return text; }
    public String getText() { return text.get(); }
    public void setText(String val) { text.set(val); }

    public DoubleProperty textSizeRatioProperty() { return textSizeRatio; }
    public double getTextSizeRatio() { return textSizeRatio.get(); }
    public void setTextSizeRatio(double val) { textSizeRatio.set(val); }

    public ObjectProperty<Node> graphicProperty() { return graphic; }
    public Node getGraphic() { return graphic.get(); }
    public void setGraphic(Node val) { graphic.set(val); }

    public BooleanProperty toggleButtonProperty() { return toggleButton; }
    public boolean getToggleButton() { return toggleButton.get(); }
    public void setToggleButton(boolean val) { toggleButton.set(val); }
}
