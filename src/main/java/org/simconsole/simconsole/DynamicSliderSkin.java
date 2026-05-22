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
            Line majorLine = new Line();
            majorLine.getStyleClass().add("slider-tick-major");
            majorLine.setStyle("-fx-stroke: #aaaaaa; -fx-stroke-width: 2px;"); // CSS Hook
            majorTickLines.add(majorLine);
            customTicksPane.getChildren().add(majorLine);

            if (slider.isShowTickLabels()) {
                Text text = new Text(String.format(java.util.Locale.US, "%.0f", val));
                text.getStyleClass().add("slider-tick-label");
                text.setStyle("-fx-fill: #aaaaaa;"); // CSS Hook
                tickLabels.add(text);
                customTicksPane.getChildren().add(text);
            }

            if (val < max && minorCount > 0) {
                double minorUnit = majorUnit / (minorCount + 1);
                for (int i = 1; i <= minorCount; i++) {
                    Line minorLine = new Line();
                    minorLine.getStyleClass().add("slider-tick-minor");
                    minorLine.setStyle("-fx-stroke: #666666; -fx-stroke-width: 1px;"); // CSS Hook
                    minorTickLines.add(minorLine);
                    customTicksPane.getChildren().add(minorLine);
                }
            }
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        boolean showTicks = getSkinnable().isShowTickMarks();
        boolean isVert = getSkinnable().getOrientation() == Orientation.VERTICAL;

        // Dividiamo lo spazio: se ci sono i tick, diamo metà spessore al track e metà ai tick
        double trackAreaThickness = isVert ? w : h;
        if (showTicks) {
            trackAreaThickness /= 2.0;
        }

        // Il track e il thumb hanno padding=0 in CSS, quindi prefSize=0x0 nativamente.
        // Dobbiamo FORZARE la loro dimensione qui prima di chiamare il layout nativo, 
        // altrimenti saranno invisibili!
        if (track instanceof javafx.scene.layout.Region && thumb instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region t = (javafx.scene.layout.Region) track;
            javafx.scene.layout.Region th = (javafx.scene.layout.Region) thumb;
            
            if (isVert) {
                t.setPrefWidth(trackAreaThickness * 0.8);
                th.setPrefWidth(trackAreaThickness * 0.9);
                th.setPrefHeight(trackAreaThickness * 0.9);
            } else {
                t.setPrefHeight(trackAreaThickness * 0.8);
                th.setPrefWidth(trackAreaThickness * 0.9);
                th.setPrefHeight(trackAreaThickness * 0.9);
            }
        }

        // Fai calcolare a SliderSkin la posizione (usando solo metà area se ci sono i tick)
        if (showTicks) {
            if (isVert) super.layoutChildren(x, y, w / 2, h);
            else super.layoutChildren(x, y, w, h / 2);
        } else {
            super.layoutChildren(x, y, w, h);
        }

        if (track == null || thumb == null) return;

        // Force perfect circular thumb and track radius dynamically
        double shortSide = Math.min(track.getLayoutBounds().getWidth(), track.getLayoutBounds().getHeight());
        double trackRadius = shortSide / 2.0;
        
        double thumbSize = Math.min(thumb.getLayoutBounds().getWidth(), thumb.getLayoutBounds().getHeight());
        double thumbRadius = thumbSize / 2.0;
        double inset2 = thumbSize * 0.04;
        double inset3 = thumbSize * 0.08;
        
        thumb.setStyle(String.format(java.util.Locale.US,
                "-fx-background-radius: %.1fpx, %.1fpx, %.1fpx;" +
                "-fx-background-insets: 0px, %.1fpx, %.1fpx;",
                thumbRadius, thumbRadius, thumbRadius, inset2, inset3));

        updateTrackGradient();
        
        if (showTicks) {
            layoutCustomTicks(x, y, w, h);
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
        
        // Prevent division by zero
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
                double centerFromLeft = thumbRad + percentage * usableTrack;
                double stopPercentage = (centerFromLeft / trackWidth) * 100.0;

                trackGradientStyle = String.format(java.util.Locale.US,
                        "-fx-background-color: linear-gradient(to right, %s %.1f%%, %s %.1f%%);",
                        colorFilled, stopPercentage, colorEmpty, stopPercentage);
            }
        }

        double shortSide = Math.min(track.getLayoutBounds().getWidth(), track.getLayoutBounds().getHeight());
        double trackRadius = shortSide / 2.0;
        String trackRadiusStyle = String.format(java.util.Locale.US,
                "-fx-background-radius: %.1fpx; -fx-border-radius: %.1fpx;",
                trackRadius, trackRadius);

        track.setStyle(trackGradientStyle + trackRadiusStyle);
    }

    private void layoutCustomTicks(double x, double y, double w, double h) {
        Slider slider = getSkinnable();
        if (majorTickLines.isEmpty()) return;

        // Pane copre tutto, così le coordinate (tx) sono relative allo slider intero
        customTicksPane.resizeRelocate(x, y, w, h);

        boolean isVert = slider.getOrientation() == Orientation.VERTICAL;
        double trackLength = isVert ? track.getLayoutBounds().getHeight() : track.getLayoutBounds().getWidth();
        double thumbLength = isVert ? thumb.getLayoutBounds().getHeight() : thumb.getLayoutBounds().getWidth();
        
        if (trackLength <= 0) return;

        double usableTrack = trackLength - thumbLength;
        double startOffset = thumbLength / 2.0;

        double min = slider.getMin();
        double max = slider.getMax();
        double range = max - min;
        
        // --- AUTO-HIDING LOGIC ---
        double pixelsPerUnit = usableTrack / range;
        double majorDistance = pixelsPerUnit * slider.getMajorTickUnit();
        double minorDistance = majorDistance / (slider.getMinorTickCount() + 1);

        // Se i minor ticks sono troppo vicini (< 4px), spegnili
        boolean showMinor = minorDistance > 4.0;
        // Se i major ticks sono troppo vicini (< 12px), nascondi il testo (salvo min e max)
        boolean showLabels = majorDistance > 12.0;

        // Proporzioni dei tick basate sullo spessore allocato all'area dei tick (metà slider)
        double tickAreaWidth = isVert ? (w / 2) : (h / 2);
        double majorLen = tickAreaWidth * 0.3; // 30% dell'area ticks
        double minorLen = tickAreaWidth * 0.15;

        // Coordinate di partenza per l'area dei tick
        double tx = isVert ? (w / 2) : 0;
        double ty = isVert ? 0 : (h / 2);

        int majorIndex = 0;
        int minorIndex = 0;

        double majorUnit = slider.getMajorTickUnit();
        int minorCount = slider.getMinorTickCount();

        for (double val = min; val <= max; val += majorUnit) {
            double percentage = (val - min) / range;
            double pos = startOffset + percentage * usableTrack;
            if (isVert) pos = trackLength - pos; // Disegna dal basso verso l'alto
            
            // Layout Major Line
            if (majorIndex < majorTickLines.size()) {
                Line line = majorTickLines.get(majorIndex);
                if (isVert) {
                    line.setStartX(tx); 
                    line.setEndX(tx + majorLen);
                    line.setStartY(track.getLayoutY() + pos); 
                    line.setEndY(track.getLayoutY() + pos);
                } else {
                    line.setStartX(track.getLayoutX() + pos); 
                    line.setEndX(track.getLayoutX() + pos);
                    line.setStartY(ty); 
                    line.setEndY(ty + majorLen);
                }

                // Layout Label
                if (majorIndex < tickLabels.size()) {
                    Text text = tickLabels.get(majorIndex);
                    boolean isExtreme = (val == min || val == max);
                    text.setVisible(showLabels || isExtreme); // Auto-hiding

                    if (text.isVisible()) {
                        double fontSize = Math.max(8, tickAreaWidth * 0.35);
                        text.setStyle(String.format(java.util.Locale.US, "-fx-font-size: %.1fpx; -fx-fill: #aaaaaa;", fontSize));
                        text.applyCss();

                        double tw = text.getLayoutBounds().getWidth();
                        double th = text.getLayoutBounds().getHeight();

                        if (isVert) {
                            text.setLayoutX(tx + majorLen + 5);
                            text.setLayoutY(track.getLayoutY() + pos + th / 4);
                        } else {
                            text.setLayoutX(track.getLayoutX() + pos - tw / 2);
                            text.setLayoutY(ty + majorLen + 5 + th / 1.5);
                        }
                    }
                }
                majorIndex++;
            }

            // Layout Minor Lines
            if (val < max && minorCount > 0) {
                double minorUnit = majorUnit / (minorCount + 1);
                for (int i = 1; i <= minorCount; i++) {
                    if (minorIndex < minorTickLines.size()) {
                        Line line = minorTickLines.get(minorIndex);
                        line.setVisible(showMinor); // Auto-hiding
                        
                        if (showMinor) {
                            double minorVal = val + minorUnit * i;
                            double mPerc = (minorVal - min) / range;
                            double mPos = startOffset + mPerc * usableTrack;
                            if (isVert) mPos = trackLength - mPos;

                            if (isVert) {
                                line.setStartX(tx); 
                                line.setEndX(tx + minorLen);
                                line.setStartY(track.getLayoutY() + mPos); 
                                line.setEndY(track.getLayoutY() + mPos);
                            } else {
                                line.setStartX(track.getLayoutX() + mPos); 
                                line.setEndX(track.getLayoutX() + mPos);
                                line.setStartY(ty); 
                                line.setEndY(ty + minorLen);
                            }
                        }
                        minorIndex++;
                    }
                }
            }
        }
    }

    // --- RESPONSIVE PROPORTIONS ---
    // Questi sostituiscono i vecchi bind() nel costruttore!
    
    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
            if (height != -1) return height * 0.25; // Spessore è il 25% della lunghezza
        }
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            if (width != -1) return width * 0.25; // Spessore è il 25% della lunghezza
        }
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
            return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
        }
        return super.computeMaxWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }
        return super.computeMaxHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
}
