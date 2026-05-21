package org.simconsole.simconsole;

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
 * Skin for {@link StandardDynamicButton}.
 * It manages the visual nodes and applies responsive sizing based on the control's size.
 */
public class StandardDynamicButtonSkin extends SkinBase<StandardDynamicButton> {

    private final Rectangle outerRing = new Rectangle();
    private final Rectangle middleRing = new Rectangle();
    private final Rectangle outerKnob = new Rectangle();
    private final Rectangle ledRing = new Rectangle();
    private final Rectangle innerKnob = new Rectangle();
    private final Text textNode = new Text();

    // Container used to align all nodes in the center, acting as the layout root for the skin
    private final StackPane container = new StackPane();

    public StandardDynamicButtonSkin(StandardDynamicButton control) {
        super(control);

        // Assign CSS classes
        outerRing.getStyleClass().add("round-button-outer-ring");
        middleRing.getStyleClass().add("round-button-middle-ring");
        outerKnob.getStyleClass().add("round-button-outer-knob");
        ledRing.getStyleClass().add("round-button-led-ring");
        innerKnob.getStyleClass().add("round-button-inner-knob");
        textNode.getStyleClass().add("round-button-text");

        // Bind text to the control's text property natively provided by ButtonBase
        textNode.textProperty().bind(control.textProperty());

        // Setup the container
        container.getChildren().addAll(outerRing, middleRing, outerKnob, ledRing, innerKnob, textNode);

        // Add the container to the skin
        getChildren().add(container);

        // Manage dynamic graphic from ButtonBase
        control.graphicProperty().addListener((obs, oldNode, newNode) -> updateGraphic(oldNode, newNode));
        updateGraphic(null, control.getGraphic());

        // Setup responsive bindings for size
        setupResponsiveBindings(control);

        // Listen for visual state changes (armed or selected)
        control.armedProperty().addListener((obs, old, armed) -> updateVisualState());
        control.selectedProperty().addListener((obs, old, selected) -> updateVisualState());

        // Gestione dell'interattività: SkinBase non implementa il comportamento del mouse di default.
        // Aggiungiamo i listener per simulare il comportamento nativo di un bottone.
        container.setOnMousePressed(e -> {
            control.arm();
        });
        
        container.setOnMouseReleased(e -> {
            boolean wasArmed = control.isArmed();
            control.disarm();
            // Lancia l'evento action solo se il mouse viene rilasciato all'interno del bottone
            if (wasArmed && container.getLayoutBounds().contains(e.getX(), e.getY())) {
                control.fire();
            }
        });

        container.setOnMouseExited(e -> {
            // Se usciamo dal bottone mentre teniamo premuto, si "disarma"
            if (control.isArmed()) {
                control.disarm();
            }
        });

        container.setOnMouseEntered(e -> {
            // Se rientriamo col mouse tenendo ancora premuto, si riarma
            if (e.isPrimaryButtonDown()) {
                control.arm();
            }
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

    private void setupResponsiveBindings(StandardDynamicButton control) {
        // We bind the dimensions based on the container's available size.
        // StackPane resizes its bounds to the Control's available layout area.
        NumberBinding minDim = Bindings.min(container.widthProperty(), container.heightProperty());

        bindRect(outerRing, minDim, 0.90, 0.20);
        bindRect(middleRing, minDim, 0.86, 0.18);
        bindRect(outerKnob, minDim, 0.82, 0.16);
        bindRect(ledRing, minDim, 0.72, 0.12);
        bindRect(innerKnob, minDim, 0.66, 0.10);

        // Handle text scaling
        container.layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            updateTextSize(bounds.getWidth(), bounds.getHeight(), control.getTextSizeRatio());
        });
        control.textSizeRatioProperty().addListener((obs, oldRatio, newRatio) -> {
            updateTextSize(container.getWidth(), container.getHeight(), newRatio.doubleValue());
        });
    }

    private void bindRect(Rectangle rect, NumberBinding minDim, double sizeFactor, double arcFactor) {
        rect.widthProperty().bind(minDim.multiply(sizeFactor));
        rect.heightProperty().bind(minDim.multiply(sizeFactor));
        rect.arcWidthProperty().bind(minDim.multiply(arcFactor));
        rect.arcHeightProperty().bind(minDim.multiply(arcFactor));
    }

    private void updateTextSize(double w, double h, double ratio) {
        double size = Math.min(w, h);
        double fontSize = size * ratio;
        if (fontSize > 0) {
            String fontStyle = String.format(java.util.Locale.US, "-fx-font-size: %.1fpx;", fontSize);
            textNode.setStyle(fontStyle);
            textNode.applyCss();
        }
    }

    private void updateVisualState() {
        StandardDynamicButton control = getSkinnable();
        boolean shouldBeLit;

        if (control.getToggleMode()) {
            // Se in modalità toggle, si accende se è selezionato O se l'utente lo sta premendo
            shouldBeLit = control.isSelected() || control.isArmed();
        } else {
            // In modalità bottone normale, si accende solo mentre viene premuto
            shouldBeLit = control.isArmed();
        }

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
        // Lay out the container to fill the skin's allocated area.
        layoutInArea(container, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return leftInset + rightInset + 30; // Minima dimensione sensata
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return topInset + bottomInset + 30; // Minima dimensione sensata
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (height != -1) {
            // Se l'altezza è nota (es. HBox fillHeight), la larghezza deve essere uguale per restare un quadrato
            return height; 
        }
        return leftInset + rightInset + 50; 
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (width != -1) {
            // Se la larghezza è nota (es. VBox fillWidth), l'altezza deve essere uguale
            return width;
        }
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
