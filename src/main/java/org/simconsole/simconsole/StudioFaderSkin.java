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

public class StudioFaderSkin extends SliderSkin {

    private Node track;
    private Node thumb;
    private final Pane customTicksPane;

    private final List<Line> leftMajorTicks = new ArrayList<>();
    private final List<Line> rightMajorTicks = new ArrayList<>();
    private final List<Line> leftMinorTicks = new ArrayList<>();
    private final List<Line> rightMinorTicks = new ArrayList<>();
    private final List<Text> leftTickLabels = new ArrayList<>();
    private final List<Text> rightTickLabels = new ArrayList<>();

    public StudioFaderSkin(StudioFader slider) {
        super(slider);

        for (Node n : getChildren()) {
            if (n.getStyleClass().contains("track")) track = n;
            else if (n.getStyleClass().contains("thumb")) thumb = n;
            else if (n.getStyleClass().contains("axis")) {
                n.setVisible(false);
                n.setManaged(false);
            }
        }

        customTicksPane = new Pane();
        customTicksPane.setPickOnBounds(false);
        getChildren().add(0, customTicksPane); // Sotto a tutto

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
        leftMajorTicks.clear();
        rightMajorTicks.clear();
        leftMinorTicks.clear();
        rightMinorTicks.clear();
        leftTickLabels.clear();
        rightTickLabels.clear();

        Slider slider = getSkinnable();
        if (!slider.isShowTickMarks()) return;

        double min = slider.getMin();
        double max = slider.getMax();
        double majorUnit = slider.getMajorTickUnit();
        int minorCount = slider.getMinorTickCount();

        if (majorUnit <= 0) majorUnit = (max - min) / 4.0;
        if (majorUnit <= 0) return;

        for (double val = min; val <= max + 0.0001; val += majorUnit) {
            Line leftLine = new Line();
            Line rightLine = new Line();
            leftMajorTicks.add(leftLine);
            rightMajorTicks.add(rightLine);
            customTicksPane.getChildren().addAll(leftLine, rightLine);

            if (slider.isShowTickLabels()) {
                // Formatting decibels or pure values
                String labelStr = String.format(java.util.Locale.US, "%.0f", val);
                // The image had specific formatting, +12, -12, etc., but we use the value directly
                Text leftText = new Text(labelStr);
                Text rightText = new Text(labelStr);
                leftText.setTextOrigin(javafx.geometry.VPos.CENTER);
                rightText.setTextOrigin(javafx.geometry.VPos.CENTER);
                leftTickLabels.add(leftText);
                rightTickLabels.add(rightText);
                customTicksPane.getChildren().addAll(leftText, rightText);
            }

            if (val < max && minorCount > 0) {
                for (int i = 1; i <= minorCount; i++) {
                    Line mLeft = new Line();
                    Line mRight = new Line();
                    leftMinorTicks.add(mLeft);
                    rightMinorTicks.add(mRight);
                    customTicksPane.getChildren().addAll(mLeft, mRight);
                }
            }
        }
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        if (track == null || thumb == null) {
            super.layoutChildren(x, y, w, h);
            return;
        }

        boolean isVert = getSkinnable().getOrientation() == Orientation.VERTICAL;

        // --- TRACK SIZE (Thin Groove) ---
        double trackW = isVert ? Math.max(3, w * 0.04) : w;
        double trackH = isVert ? h : Math.max(3, h * 0.04);

        // --- THUMB SIZE (Rectangular Fader) ---
        double thumbW = isVert ? w * 0.40 : h * 0.80;
        double thumbH = isVert ? w * 0.80 : h * 0.40;
        thumbW = Math.max(10, thumbW);
        thumbH = Math.max(10, thumbH);

        // Forza nativamente le misure per garantire corretto calcolo del click del mouse
        if (track instanceof javafx.scene.layout.Region && thumb instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region t = (javafx.scene.layout.Region) track;
            javafx.scene.layout.Region th = (javafx.scene.layout.Region) thumb;
            t.setPrefWidth(trackW);
            t.setPrefHeight(trackH);
            th.setPrefWidth(thumbW);
            th.setPrefHeight(thumbH);
        }

        super.layoutChildren(x, y, w, h);

        double trackX = isVert ? x + w / 2.0 - trackW / 2.0 : x;
        double trackY = isVert ? y : y + h / 2.0 - trackH / 2.0;

        track.resizeRelocate(trackX, trackY, trackW, trackH);
        
        // Track Style: un solco profondo nel metallo
        track.setStyle("-fx-background-color: #050505; -fx-effect: innershadow(gaussian, rgba(0,0,0,0.9), 5, 0, 0, 2); -fx-background-radius: 2px;");

        thumb.resize(thumbW, thumbH);
        applyThumbStyle(thumbW, thumbH, isVert);

        // Layout calcolato percentualmente sul range utilizzabile
        double min = getSkinnable().getMin();
        double max = getSkinnable().getMax();
        double val = getSkinnable().getValue();
        double range = max - min;
        double percentage = range > 0 ? (val - min) / range : 0;
        percentage = Math.max(0, Math.min(1, percentage));

        double usableTrack = isVert ? trackH - thumbH : trackW - thumbW;
        usableTrack = Math.max(0, usableTrack);
        double startOffset = (isVert ? thumbH : thumbW) / 2.0;

        if (isVert) {
            double thumbCenterY = trackY + startOffset + (1.0 - percentage) * usableTrack;
            double thumbX = x + w / 2.0 - thumbW / 2.0;
            thumb.relocate(thumbX, thumbCenterY - thumbH / 2.0);
        } else {
            double thumbCenterX = trackX + startOffset + percentage * usableTrack;
            double thumbY = y + h / 2.0 - thumbH / 2.0;
            thumb.relocate(thumbCenterX - thumbW / 2.0, thumbY);
        }

        layoutTicks(x, y, w, h, isVert, startOffset, usableTrack, trackW, trackH, min, max, range);
    }

    private void applyThumbStyle(double tw, double th, boolean isVert) {
        String imgPath = isVert ? "img/thumb_fader.png" : "img/thumb_fader_h.png";
        String imgUrl = getClass().getResource(imgPath).toExternalForm();
        
        String style = String.format(java.util.Locale.US,
            "-fx-background-image: url('%s');" +
            "-fx-background-size: 100%% 100%%;" +
            "-fx-background-repeat: no-repeat;" +
            "-fx-background-position: center;" +
            "-fx-background-color: transparent;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 8, 0, 0, 4);",
            imgUrl
        );
        
        thumb.setRotate(0);
        thumb.setStyle(style);
    }

    private void layoutTicks(double x, double y, double w, double h, boolean isVert, 
                             double startOffset, double usableTrack, double trackW, double trackH, 
                             double min, double max, double range) {
        if (!getSkinnable().isShowTickMarks()) {
            customTicksPane.setVisible(false);
            return;
        }
        customTicksPane.setVisible(true);
        customTicksPane.resizeRelocate(x, y, w, h);

        double majorUnit = getSkinnable().getMajorTickUnit();
        if (majorUnit <= 0) majorUnit = range / 4.0;
        if (majorUnit <= 0) majorUnit = 1;
        int minorCount = getSkinnable().getMinorTickCount();

        double tickLength = isVert ? w * 0.12 : h * 0.12;
        double minorLength = tickLength * 0.5;
        double trackCenter = isVert ? x + w / 2.0 : y + h / 2.0;
        
        // Gap fra il solco (track) e l'inizio delle tacche
        double gap = isVert ? trackW / 2.0 + w * 0.03 : trackH / 2.0 + h * 0.03;

        // DynamicSlider standards for responsiveness
        double majorDistance = (majorUnit / range) * usableTrack;
        boolean showLabels = majorDistance > 12.0;
        boolean showMinor = (majorDistance / (minorCount + 1)) > 4.0;

        double maxFontByThickness = isVert ? (w * 0.25) : (h * 0.25);
        double maxFontBySpacing = isVert ? (majorDistance * 0.8) : (majorDistance * 0.45);
        if (!showLabels) {
            maxFontBySpacing = isVert ? (usableTrack * 0.3) : (usableTrack * 0.2);
        }
        double idealFontSize = Math.min(maxFontByThickness, maxFontBySpacing);
        double fontSize = Math.max(1, idealFontSize); 

        double majorStroke = Math.max(1.0, fontSize * 0.15);
        double minorStroke = Math.max(1.0, fontSize * 0.08);

        int majorIndex = 0;
        int minorIndex = 0;

        for (double v = min; v <= max + 0.0001; v += majorUnit) {
            double vPerc = range > 0 ? (v - min) / range : 0;
            vPerc = Math.max(0, Math.min(1, vPerc));
            double pos = startOffset + (isVert ? (1.0 - vPerc) : vPerc) * usableTrack;

            if (majorIndex < leftMajorTicks.size()) {
                Line leftL = leftMajorTicks.get(majorIndex);
                Line rightL = rightMajorTicks.get(majorIndex);
                
                leftL.setStyle(String.format(java.util.Locale.US, "-fx-stroke: #dddddd; -fx-stroke-width: %.1fpx; -fx-stroke-line-cap: butt;", majorStroke));
                rightL.setStyle(String.format(java.util.Locale.US, "-fx-stroke: #dddddd; -fx-stroke-width: %.1fpx; -fx-stroke-line-cap: butt;", majorStroke));

                if (isVert) {
                    double py = y + pos;
                    leftL.setStartX(trackCenter - gap - tickLength);
                    leftL.setEndX(trackCenter - gap);
                    leftL.setStartY(py); leftL.setEndY(py);

                    rightL.setStartX(trackCenter + gap);
                    rightL.setEndX(trackCenter + gap + tickLength);
                    rightL.setStartY(py); rightL.setEndY(py);
                } else {
                    double px = x + pos;
                    leftL.setStartY(trackCenter - gap - tickLength);
                    leftL.setEndY(trackCenter - gap);
                    leftL.setStartX(px); leftL.setEndX(px);

                    rightL.setStartY(trackCenter + gap);
                    rightL.setEndY(trackCenter + gap + tickLength);
                    rightL.setStartX(px); rightL.setEndX(px);
                }

                if (majorIndex < leftTickLabels.size()) {
                    Text leftText = leftTickLabels.get(majorIndex);
                    Text rightText = rightTickLabels.get(majorIndex);
                    
                    boolean isExtreme = (v == min || Math.abs(v - max) < 0.001);
                    boolean visible = showLabels || isExtreme;
                    leftText.setVisible(visible);
                    rightText.setVisible(visible);

                    if (visible) {
                        leftText.setStyle(String.format(java.util.Locale.US, "-fx-font-size: %.1fpx; -fx-fill: #cccccc;", fontSize));
                        rightText.setStyle(String.format(java.util.Locale.US, "-fx-font-size: %.1fpx; -fx-fill: #cccccc;", fontSize));
                        
                        leftText.applyCss(); 
                        rightText.applyCss();
                        
                        double twL = leftText.getLayoutBounds().getWidth();
                        double twR = rightText.getLayoutBounds().getWidth();
                        double th = leftText.getLayoutBounds().getHeight();
                        double textGap = fontSize * 0.5;

                        if (isVert) {
                            leftText.setLayoutX(trackCenter - gap - tickLength - textGap - twL);
                            leftText.setLayoutY(y + pos);
                            rightText.setLayoutX(trackCenter + gap + tickLength + textGap);
                            rightText.setLayoutY(y + pos);
                        } else {
                            leftText.setLayoutX(x + pos - twL / 2.0);
                            leftText.setLayoutY(trackCenter - gap - tickLength - textGap - th / 2.0);
                            rightText.setLayoutX(x + pos - twR / 2.0);
                            rightText.setLayoutY(trackCenter + gap + tickLength + textGap + th / 2.0);
                        }
                    }
                }
                majorIndex++;
            }

            if (v < max && minorCount > 0) {
                double minorUnitVal = majorUnit / (minorCount + 1);
                for (int i = 1; i <= minorCount; i++) {
                    if (minorIndex < leftMinorTicks.size()) {
                        Line leftM = leftMinorTicks.get(minorIndex);
                        Line rightM = rightMinorTicks.get(minorIndex);
                        
                        leftM.setVisible(showMinor);
                        rightM.setVisible(showMinor);
                        
                        if (showMinor) {
                            leftM.setStyle(String.format(java.util.Locale.US, "-fx-stroke: #888888; -fx-stroke-width: %.1fpx;", minorStroke));
                            rightM.setStyle(String.format(java.util.Locale.US, "-fx-stroke: #888888; -fx-stroke-width: %.1fpx;", minorStroke));

                            double minorVal = v + minorUnitVal * i;
                            double mPerc = range > 0 ? (minorVal - min) / range : 0;
                            mPerc = Math.max(0, Math.min(1, mPerc));
                            double mPos = startOffset + (isVert ? (1.0 - mPerc) : mPerc) * usableTrack;

                            if (isVert) {
                                double py = y + mPos;
                                leftM.setStartX(trackCenter - gap - minorLength);
                                leftM.setEndX(trackCenter - gap);
                                leftM.setStartY(py); leftM.setEndY(py);

                                rightM.setStartX(trackCenter + gap);
                                rightM.setEndX(trackCenter + gap + minorLength);
                                rightM.setStartY(py); rightM.setEndY(py);
                            } else {
                                double px = x + mPos;
                                leftM.setStartY(trackCenter - gap - minorLength);
                                leftM.setEndY(trackCenter - gap);
                                leftM.setStartX(px); leftM.setEndX(px);

                                rightM.setStartY(trackCenter + gap);
                                rightM.setEndY(trackCenter + gap + minorLength);
                                rightM.setStartX(px); rightM.setEndX(px);
                            }
                        }
                        minorIndex++;
                    }
                }
            }
        }
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.VERTICAL) {
            double h = height != -1 ? height : getSkinnable().getHeight();
            return (h > 0 ? h : 300) * 0.35;
        }
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            double w = width != -1 ? width : getSkinnable().getWidth();
            return (w > 0 ? w : 300) * 0.35;
        }
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) { return 0; }
    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) { return 0; }
}
