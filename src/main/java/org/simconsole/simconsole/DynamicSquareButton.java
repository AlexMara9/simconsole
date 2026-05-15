package org.simconsole.simconsole;

import javafx.beans.DefaultProperty;
import javafx.beans.property.*;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

@DefaultProperty("graphic")
public class DynamicSquareButton extends StackPane {
    private final StringProperty text = new SimpleStringProperty("");
    private final DoubleProperty textSizeRatio = new SimpleDoubleProperty(0.15);
    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();
    private final BooleanProperty toggleButton = new SimpleBooleanProperty(false);

    private final Rectangle outerRing = new Rectangle();
    private final Rectangle middleRing = new Rectangle();
    private final Rectangle outerKnob = new Rectangle();
    private final Rectangle ledRing = new Rectangle();
    private final Rectangle innerKnob = new Rectangle();
    private final Text textNode = new Text();
    private final Rectangle hitBox = new Rectangle();

    public DynamicSquareButton() {
        this.getStyleClass().add("dynamic-square-button");

        // Riutilizziamo le stesse classi CSS del bottone rotondo dato che gestiscono solo i colori
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
                // Posiziona la grafica dietro l'hitBox per non bloccare i click
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
        var minDim = javafx.beans.binding.Bindings.min(this.widthProperty(), this.heightProperty());
        
        // I moltiplicatori sono il doppio rispetto ai raggi (radius) usati nel bottone rotondo
        // Aggiungiamo anche un arcFactor proporzionale per smussare gli angoli del quadrato
        bindRect(outerRing, minDim, 0.90, 0.20);
        bindRect(middleRing, minDim, 0.86, 0.18);
        bindRect(outerKnob, minDim, 0.82, 0.16);
        bindRect(ledRing, minDim, 0.72, 0.12);
        bindRect(innerKnob, minDim, 0.66, 0.10);
        
        bindRect(hitBox, minDim, 0.90, 0.20);

        this.layoutBoundsProperty().addListener((obs, old, bounds) -> updateLayout(bounds));
        textSizeRatio.addListener((obs, old, val) -> updateLayout(this.getLayoutBounds()));
    }

    private void bindRect(Rectangle rect, javafx.beans.binding.NumberBinding minDim, double sizeFactor, double arcFactor) {
        rect.widthProperty().bind(minDim.multiply(sizeFactor));
        rect.heightProperty().bind(minDim.multiply(sizeFactor));
        rect.arcWidthProperty().bind(minDim.multiply(arcFactor));
        rect.arcHeightProperty().bind(minDim.multiply(arcFactor));
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
            } else if(toggleButton.get()){
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
