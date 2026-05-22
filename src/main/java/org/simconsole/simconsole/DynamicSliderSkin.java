package org.simconsole.simconsole;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class DynamicSliderSkin extends SliderSkin {

    private Node track;
    private Node thumb;
    private final Pane customTicksPane;

    private final List<Line> majorTickLines = new ArrayList<>();
    private final List<Line> minorTickLines = new ArrayList<>();
    private final List<Text> tickLabels = new ArrayList<>();

    public DynamicSliderSkin(DynamicSlider slider) {
        super(slider);

        // Find the native nodes created by SliderSkin
        for (Node n : getChildren()) {
            if (n.getStyleClass().contains("track")) track = n;
            else if (n.getStyleClass().contains("thumb")) thumb = n;
            else if (n.getStyleClass().contains("axis")) {
                n.setVisible(false); // Hide native axis completely
                n.setManaged(false);
            }
        }

        customTicksPane = new Pane();
        customTicksPane.setPickOnBounds(false);
        // Insert custom pane before the track so it sits underneath
        getChildren().add(0, customTicksPane);

        // Listeners for dynamic gradient updates
        slider.valueProperty().addListener((obs, old, val) -> updateTrackGradient());
        slider.minProperty().addListener((obs, old, val) -> updateTrackGradient());
        slider.maxProperty().addListener((obs, old, val) -> updateTrackGradient());

        // Rebuild ticks when important properties change
        rebuildCustomTicks();
        slider.majorTickUnitProperty().addListener(e -> rebuildCustomTicks());
        slider.minorTickCountProperty().addListener(e -> rebuildCustomTicks());
        slider.showTickMarksProperty().addListener(e -> rebuildCustomTicks());
        slider.showTickLabelsProperty().addListener(e -> rebuildCustomTicks());
        slider.minProperty().addListener(e -> rebuildCustomTicks());
        slider.maxProperty().addListener(e -> rebuildCustomTicks());
    }

    private void rebuildCustomTicks() {
        customTicksPane.getChildren().clear();
        majorTickLines.clear();
        minorTickLines.clear();
        tickLabels.clear();

        Slider slider = getSkinnable();
        if (!slider.isShowTickMarks()) return;

        double min = slider.getMin();
        double max = slider.getMax();
        double majorUnit = slider.getMajorTickUnit();
        int minorCount = slider.getMinorTickCount();

        if (majorUnit <= 0) majorUnit = (max - min) / 4.0;
        if (majorUnit <= 0) return;

        for (double val = min; val <= max; val += majorUnit) {
            System.out.println(val);
            Line majorLine = new Line();
            majorLine.getStyleClass().add("slider-tick-major");
            majorLine.setStyle("-fx-stroke: #aaaaaa; -fx-stroke-width: 2px;"); // CSS Hook
            majorTickLines.add(majorLine);
            customTicksPane.getChildren().add(majorLine);

            if (slider.isShowTickLabels()) {
                Text text = new Text(Double.toString(val));
                text.getStyleClass().add("slider-tick-label");
                text.setTextOrigin(javafx.geometry.VPos.CENTER); // Garantisce centering verticale corretto
                tickLabels.add(text);
                customTicksPane.getChildren().add(text);
            }

            if (val < max && minorCount > 0) {
                double minorUnit = majorUnit / (minorCount + 1);
                for (int i = 1; i <= minorCount; i++) {
                    Line minorLine = new Line();
                    minorLine.getStyleClass().add("slider-tick-minor");
                    minorTickLines.add(minorLine);
                    customTicksPane.getChildren().add(minorLine);
                }
            }
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);
        if (track == null || thumb == null) return;

        boolean showTicks = getSkinnable().isShowTickMarks();
        boolean isVert = getSkinnable().getOrientation() == Orientation.VERTICAL;

        double trackAreaW = isVert ? (showTicks ? w * 0.4 : w) : w;
        double trackAreaH = isVert ? h : (showTicks ? h * 0.4 : h);

        double tickAreaW = isVert ? (showTicks ? w * 0.6 : 0) : w;
        double tickAreaH = isVert ? h : (showTicks ? h * 0.6 : 0);

        // --- THUMB SIZE ---
        // Il thumb cerca di occupare il 25% della lunghezza totale, ma si restringe se lo spazio è minore
        double idealThickness = isVert ? h * 0.25 : w * 0.25;
        double maxAvailable = isVert ? trackAreaW : trackAreaH;
        double thumbSize = Math.min(idealThickness, maxAvailable);
        thumbSize = Math.max(1, thumbSize);

        // --- TRACK LAYOUT ---
        // Lo spessore del track prende il 90% del thumb
        double trackW = isVert ? (thumbSize * 0.9) : trackAreaW;
        double trackH = isVert ? trackAreaH : (thumbSize * 0.9);
        
        trackW = Math.max(1, trackW);
        trackH = Math.max(1, trackH);

        double trackX = isVert ? x + (trackAreaW - trackW) / 2.0 : x;
        double trackY = isVert ? y : y + (trackAreaH - trackH) / 2.0;

        track.resizeRelocate(trackX, trackY, trackW, trackH);

        // --- THUMB RADII ---
        thumb.resize(thumbSize, thumbSize);
        
        double thumbRadius = Math.max(0, thumbSize / 2.0);
        double inset2 = thumbSize * 0.04;
        double inset3 = thumbSize * 0.08;
        
        thumb.setStyle(String.format(java.util.Locale.US,
                "-fx-background-radius: %.1fpx, %.1fpx, %.1fpx;" +
                "-fx-background-insets: 0px, %.1fpx, %.1fpx;",
                thumbRadius, thumbRadius, thumbRadius, inset2, inset3));

        double min = getSkinnable().getMin();
        double max = getSkinnable().getMax();
        double val = getSkinnable().getValue();
        double range = max - min;
        double percentage = range > 0 ? (val - min) / range : 0;
        percentage = Math.max(0, Math.min(1, percentage));

        double usableTrack = isVert ? trackH - thumbSize : trackW - thumbSize;
        usableTrack = Math.max(0, usableTrack);
        double startOffset = thumbSize / 2.0;

        if (isVert) {
            double thumbCenterY = trackY + startOffset + (1.0 - percentage) * usableTrack;
            double thumbX = trackX + trackW / 2.0 - thumbSize / 2.0;
            thumb.relocate(thumbX, thumbCenterY - thumbSize / 2.0);
        } else {
            double thumbCenterX = trackX + startOffset + percentage * usableTrack;
            double thumbY = trackY + trackH / 2.0 - thumbSize / 2.0;
            thumb.relocate(thumbCenterX - thumbSize / 2.0, thumbY);
        }

        updateTrackGradient(); // Aggiorna i colori usando le nuove geometrie

        if (showTicks) {
            customTicksPane.setVisible(true);
            customTicksPane.resizeRelocate(x, y, w, h);

            double tx = isVert ? x + trackAreaW : x;
            double ty = isVert ? y : y + trackAreaH;

            double majorUnit = getSkinnable().getMajorTickUnit();
            if (majorUnit <= 0) majorUnit = range / 4.0;
            if (majorUnit <= 0) majorUnit = 1;
            int minorCount = getSkinnable().getMinorTickCount();
            
            double majorDistance = (majorUnit / range) * usableTrack;
            boolean showLabels = majorDistance > 12.0;
            boolean showMinor = (majorDistance / (minorCount + 1)) > 4.0;

            double majorLen = isVert ? tickAreaW * 0.25 : tickAreaH * 0.25;
            double minorLen = isVert ? tickAreaW * 0.12 : tickAreaH * 0.12;
            double textMargin = isVert ? tickAreaW * 0.05 : tickAreaH * 0.05; // Gap responsivo!

            double maxFontByThickness = isVert ? (tickAreaW * 0.35) : (tickAreaH * 0.6);
            double maxFontBySpacing = isVert ? (majorDistance * 0.8) : (majorDistance * 0.45);
            if (!showLabels) {
                maxFontBySpacing = isVert ? (usableTrack * 0.3) : (usableTrack * 0.2);
            }
            double idealFontSize = Math.min(maxFontByThickness, maxFontBySpacing);
            // Rimossi tutti i limiti minimi per consentire un rimpicciolimento infinito
            double fontSize = Math.max(1, idealFontSize); 

            double majorStroke = Math.max(1.0, fontSize * 0.15);
            double minorStroke = Math.max(1.0, fontSize * 0.08);

            int majorIndex = 0;
            int minorIndex = 0;

            for (double v = min; v <= max + 0.0001; v += majorUnit) {
                double vPerc = range > 0 ? (v - min) / range : 0;
                vPerc = Math.max(0, Math.min(1, vPerc));
                double pos = startOffset + (isVert ? (1.0 - vPerc) : vPerc) * usableTrack;

                if (majorIndex < majorTickLines.size()) {
                    Line line = majorTickLines.get(majorIndex);
                    line.setStyle(String.format(java.util.Locale.US, "-fx-stroke: #aaaaaa; -fx-stroke-width: %.1fpx;", majorStroke));
                    if (isVert) {
                        line.setStartX(tx);
                        line.setEndX(tx + majorLen);
                        line.setStartY(trackY + pos);
                        line.setEndY(trackY + pos);
                    } else {
                        line.setStartX(trackX + pos);
                        line.setEndX(trackX + pos);
                        line.setStartY(ty);
                        line.setEndY(ty + majorLen);
                    }

                    if (majorIndex < tickLabels.size()) {
                        Text text = tickLabels.get(majorIndex);
                        boolean isExtreme = (v == min || Math.abs(v - max) < 0.001);
                        text.setVisible(showLabels || isExtreme);

                        if (text.isVisible()) {
                            text.setStyle(String.format(java.util.Locale.US, "-fx-font-size: %.1fpx; -fx-fill: #aaaaaa;", fontSize));
                            text.applyCss();
                            double tw = text.getLayoutBounds().getWidth();
                            double th = text.getLayoutBounds().getHeight();

                            if (isVert) {
                                double textSpaceX = tx + majorLen + textMargin;
                                double textSpaceW = tickAreaW - majorLen - textMargin;
                                double calculatedX = textSpaceX + textSpaceW / 2.0 - tw / 2.0;
                                // Clampa a sinistra per impedire FISICAMENTE che il testo tocchi la tacca
                                text.setLayoutX(Math.max(textSpaceX, calculatedX));
                                text.setLayoutY(trackY + pos); 
                            } else {
                                double textSpaceY = ty + majorLen + textMargin;
                                double textSpaceH = tickAreaH - majorLen - textMargin;
                                double calculatedY = textSpaceY + textSpaceH / 2.0;
                                // Clampa in alto per impedire FISICAMENTE che il testo tocchi la tacca
                                text.setLayoutY(Math.max(textSpaceY + th / 2.0, calculatedY));
                                text.setLayoutX(trackX + pos - tw / 2.0);
                            }
                        }
                    }
                    majorIndex++;
                }

                if (v < max && minorCount > 0) {
                    double minorUnitVal = majorUnit / (minorCount + 1);
                    for (int i = 1; i <= minorCount; i++) {
                        if (minorIndex < minorTickLines.size()) {
                            Line line = minorTickLines.get(minorIndex);
                            line.setVisible(showMinor);
                            if (showMinor) {
                                line.setStyle(String.format(java.util.Locale.US, "-fx-stroke: #666666; -fx-stroke-width: %.1fpx;", minorStroke));
                                double minorVal = v + minorUnitVal * i;
                                double mPerc = range > 0 ? (minorVal - min) / range : 0;
                                mPerc = Math.max(0, Math.min(1, mPerc));
                                double mPos = startOffset + (isVert ? (1.0 - mPerc) : mPerc) * usableTrack;

                                if (isVert) {
                                    line.setStartX(tx);
                                    line.setEndX(tx + minorLen);
                                    line.setStartY(trackY + mPos);
                                    line.setEndY(trackY + mPos);
                                } else {
                                    line.setStartX(trackX + mPos);
                                    line.setEndX(trackX + mPos);
                                    line.setStartY(ty);
                                    line.setEndY(ty + minorLen);
                                }
                            }
                            minorIndex++;
                        }
                    }
                }
            }
        } else {
            customTicksPane.getChildren().forEach(n -> n.setVisible(false));
        }
    }

    private String trackGradientStyle = "";

    private void updateTrackGradient() {
        Slider slider = getSkinnable();
        if (track == null || thumb == null) return;

        double min = slider.getMin();
        double max = slider.getMax();
        double val = slider.getValue();
        
        if (max == min) return;
        
        double percentage = (val - min) / (max - min);
        percentage = Math.max(0, Math.min(1, percentage));

        String colorFilled = "-track-filled";
        String colorEmpty = "-track-empty";

        if (slider.getOrientation() == Orientation.VERTICAL) {
            double trackHeight = track.getLayoutBounds().getHeight();
            double thumbHeight = thumb.getLayoutBounds().getHeight();
            if (trackHeight > 0 && thumbHeight > 0) {
                double thumbRad = thumbHeight / 2.0;
                double usableTrack = trackHeight - thumbHeight;
                usableTrack = Math.max(0, usableTrack);
                double centerFromTop = thumbRad + (1.0 - percentage) * usableTrack;
                double stopPercentage = (centerFromTop / trackHeight) * 100.0;

                trackGradientStyle = String.format(java.util.Locale.US,
                        "-fx-background-color: linear-gradient(to bottom, %s %.1f%%, %s %.1f%%);",
                        colorEmpty, stopPercentage, colorFilled, stopPercentage);
            }
        } else {
            double trackWidth = track.getLayoutBounds().getWidth();
            double thumbWidth = thumb.getLayoutBounds().getWidth();
            if (trackWidth > 0 && thumbWidth > 0) {
                double thumbRad = thumbWidth / 2.0;
                double usableTrack = trackWidth - thumbWidth;
                usableTrack = Math.max(0, usableTrack);
                double centerFromLeft = thumbRad + percentage * usableTrack;
                double stopPercentage = (centerFromLeft / trackWidth) * 100.0;

                trackGradientStyle = String.format(java.util.Locale.US,
                        "-fx-background-color: linear-gradient(to right, %s %.1f%%, %s %.1f%%);",
                        colorFilled, stopPercentage, colorEmpty, stopPercentage);
            }
        }

        double shortSide = Math.min(track.getLayoutBounds().getWidth(), track.getLayoutBounds().getHeight());
        double trackRadius = Math.max(0, shortSide / 2.0);
        String trackRadiusStyle = String.format(java.util.Locale.US,
                "-fx-background-radius: %.1fpx; -fx-border-radius: %.1fpx;",
                trackRadius, trackRadius);

        track.setStyle(trackGradientStyle + trackRadiusStyle);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
            double h = height != -1 ? height : getSkinnable().getHeight();
            return (h > 0 ? h : 200) * 0.25;
        }
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            double w = width != -1 ? width : getSkinnable().getWidth();
            return (w > 0 ? w : 200) * 0.25;
        }
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 0; // Sblocca la compressione infinita
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return 0; // Sblocca la compressione infinita
    }
}
