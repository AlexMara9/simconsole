package org.simconsole.simconsole;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * Standard JavaFX Skin for ResponsiveLabel.
 * Dynamically resizes the font based on the layout bounds.
 */
public class ResponsiveLabelSkin extends SkinBase<ResponsiveLabel> {

    private final Text textNode = new Text();
    private final StackPane container = new StackPane();

    public ResponsiveLabelSkin(ResponsiveLabel control) {
        super(control);

        // Allow container to shrink
        container.setMinSize(0, 0);

        textNode.getStyleClass().add("responsive-text");
        textNode.textProperty().bind(control.textProperty());
        
        // Bind text color to the control's text fill
        textNode.fillProperty().bind(control.textFillProperty());

        container.getChildren().add(textNode);
        getChildren().add(container);

        setupResponsiveBindings(control);
    }

    private void setupResponsiveBindings(ResponsiveLabel control) {
        // Sblocca la compressione infinita per evitare overflow nel layout padre
        container.setMinSize(0, 0);

        container.layoutBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            updateFontSize(bounds.getWidth(), bounds.getHeight(), control.getTextSizeRatio());
        });

        control.textSizeRatioProperty().addListener((obs, oldRatio, newRatio) -> {
            updateFontSize(container.getWidth(), container.getHeight(), newRatio.doubleValue());
        });
        
        // Initial setup
        updateFontSize(container.getWidth(), container.getHeight(), control.getTextSizeRatio());
    }

    private void updateFontSize(double w, double h, double ratio) {
        if (Double.isNaN(w) || w <= 0 || Double.isNaN(h) || h <= 0) return;
        
        // Usiamo la larghezza (w) per la proporzione, limitata dall'altezza per evitare che diventi troppo grande
        double size = Math.min(w, h * 2.0); 
        double calculatedSize = size * ratio;
        
        if (calculatedSize > 0) {
            // Bypassiamo il CSS setStyle che causa pesanti ricalcoli ("scatti") e usiamo l'API nativa
            ResponsiveLabel control = getSkinnable();
            String family = control.getFont() != null ? control.getFont().getFamily() : "Segoe UI";
            textNode.setFont(javafx.scene.text.Font.font(family, javafx.scene.text.FontWeight.BOLD, calculatedSize));
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        layoutInArea(container, contentX, contentY, contentWidth, contentHeight, -1, HPos.CENTER, VPos.CENTER);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 0; // Sblocca la compressione infinita
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 0; // Sblocca la compressione infinita
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 50; // Dimensione fissa per evitare feedback loops sul layout padre
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 20; // Dimensione fissa per evitare feedback loops sul layout padre
    }

}
