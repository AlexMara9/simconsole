package org.simconsole.simconsole.components;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.control.skin.SliderSkin;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom JavaFX skin for {@link PitchSlider} rendering a DJ pitch fader control.
 */
public class PitchSliderSkin extends SliderSkin {
    private Node track;
    private Node thumb;
    private final Pane customTicksPane;
    private final List<Line> leftMajorTicks = new ArrayList<>();
    private final List<Line> rightMajorTicks = new ArrayList<>();
    private final List<Line> leftMinorTicks = new ArrayList<>();
    private final List<Line> rightMinorTicks = new ArrayList<>();
    private final List<Text> leftTickLabels = new ArrayList<>();
    private final List<Text> rightTickLabels = new ArrayList<>();
    public PitchSliderSkin(PitchSlider slider) {
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
        getChildren().add(0, customTicksPane);
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
        leftMinorTicks.clear();
        rightMinorTicks.clear();
        leftTickLabels.clear();
        Slider slider = getSkinnable();
        if (!slider.isShowTickMarks()) return;
        double min = slider.getMin();
        double max = slider.getMax();
        double majorUnit = slider.getMajorTickUnit();
        int minorCount = slider.getMinorTickCount();
        if (majorUnit <= 0) majorUnit = (max - min) / 4.0;
        if (majorUnit <= 0) return;
        double firstMajor = Math.ceil(min / majorUnit) * majorUnit;
        for (double val = firstMajor; val <= max + 0.0001; val += majorUnit) {
            Line rightLine = new Line();
            rightMajorTicks.add(rightLine);
            customTicksPane.getChildren().add(rightLine);
            if (slider.isShowTickLabels()) {
                String labelStr;
                if (Math.abs(val - 1.0) < 0.001) labelStr = "1x";
                else labelStr = String.format(java.util.Locale.US, "%.1fx", val);
                Text leftText = new Text(labelStr);
                leftText.setTextOrigin(javafx.geometry.VPos.CENTER);
                leftTickLabels.add(leftText);
                customTicksPane.getChildren().add(leftText);
            }
            if (val < max && minorCount > 0) {
                for (int i = 1; i <= minorCount; i++) {
                    Line mRight = new Line();
                    rightMinorTicks.add(mRight);
                    customTicksPane.getChildren().add(mRight);
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
        double trackW = isVert ? Math.max(4, w * 0.06) : w;
        double trackH = isVert ? h : Math.max(4, h * 0.06);
        double thumbW = isVert ? w * 0.60 : h * 0.40;
        double thumbH = isVert ? w * 0.40 : h * 0.60;
        thumbW = Math.max(15, thumbW);
        thumbH = Math.max(15, thumbH);
        if (track instanceof javafx.scene.layout.Region && thumb instanceof javafx.scene.layout.Region) {
            javafx.scene.layout.Region t = (javafx.scene.layout.Region) track;
            javafx.scene.layout.Region th = (javafx.scene.layout.Region) thumb;
            t.setPrefWidth(trackW);
            t.setPrefHeight(trackH);
            th.setPrefWidth(thumbW);
            th.setPrefHeight(thumbH);
        }
        super.layoutChildren(x, y, w, h);
        double trackX = isVert ? x + w * 0.65 - trackW / 2.0 : x;
        double trackY = isVert ? y : y + h / 2.0 - trackH / 2.0;
        track.resizeRelocate(trackX, trackY, trackW, trackH);
        track.setStyle("-fx-background-color: #000; -fx-effect: innershadow(gaussian, rgba(0,0,0,1.0), 3, 0, 0, 1); -fx-background-radius: 1px;");
        thumb.resize(thumbW, thumbH);
        applyThumbStyle(thumbW, thumbH, isVert);
        double min = getSkinnable().getMin();
        double max = getSkinnable().getMax();
        double val = getSkinnable().getValue();
        double range = max - min;
        double percentage = range > 0 ? (val - min) / range : 0;
        percentage = Math.max(0, Math.min(1, percentage));
        double usableTrack = isVert ? trackH - thumbH : trackW - thumbW;
        usableTrack = Math.max(0, usableTrack);
        double startOffset = (isVert ? thumbH : thumbW) / 2.0;
        double trackCenter = isVert ? trackX + trackW / 2.0 : trackY + trackH / 2.0;
        if (isVert) {
            double thumbCenterY = trackY + startOffset + (1.0 - percentage) * usableTrack;
            double thumbX = trackCenter - thumbW * 0.15;
            thumb.relocate(thumbX, thumbCenterY - thumbH / 2.0);
        } else {
            double thumbCenterX = trackX + startOffset + percentage * usableTrack;
            double thumbY = trackCenter - thumbH * 0.15;
            thumb.relocate(thumbCenterX - thumbW / 2.0, thumbY);
        }
        layoutTicks(x, y, w, h, isVert, startOffset, usableTrack, trackW, trackH, min, max, range, trackCenter);
    }
    private void applyThumbStyle(double tw, double th, boolean isVert) {
        String style = ""
            + "-fx-background-color: linear-gradient(to bottom, #111 0%, #222 46%, #eee 48%, #eee 52%, #222 54%, #111 100%); "
            + "-fx-background-radius: 3px; "
            + "-fx-border-color: #000; "
            + "-fx-border-width: 1px; "
            + "-fx-border-radius: 3px; "
            + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.9), 6, 0, 0, 2);";
        thumb.setRotate(0);
        thumb.setStyle(style);
    }
    private void layoutTicks(double x, double y, double w, double h, boolean isVert,
                             double startOffset, double usableTrack, double trackW, double trackH,
                             double min, double max, double range, double trackCenter) {
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
        double gap = isVert ? trackW / 2.0 + w * 0.05 : trackH / 2.0 + h * 0.05;
        double majorDistance = (majorUnit / range) * usableTrack;
        boolean showLabels = majorDistance > 10.0;
        boolean showMinor = (majorDistance / (minorCount + 1)) > 4.0;
        double maxFontByThickness = isVert ? (w * 0.18) : (h * 0.18);
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
        double firstMajor = Math.ceil(min / majorUnit) * majorUnit;
        for (double v = firstMajor; v <= max + 0.0001; v += majorUnit) {
            double vPerc = range > 0 ? (v - min) / range : 0;
            vPerc = Math.max(0, Math.min(1, vPerc));
            double pos = startOffset + (isVert ? (1.0 - vPerc) : vPerc) * usableTrack;
            boolean isZero = Math.abs(v - 1.0) < 0.001;
            if (majorIndex < rightMajorTicks.size()) {
                Line rightL = rightMajorTicks.get(majorIndex);
                double stroke = isZero ? majorStroke * 1.5 : majorStroke;
                String color = isZero ? "#ffffff" : "#cccccc";
                rightL.setStyle(String.format(java.util.Locale.US, "-fx-stroke: %s; -fx-stroke-width: %.1fpx; -fx-stroke-line-cap: butt;", color, stroke));
                if (isVert) {
                    double py = y + pos;
                    rightL.setStartX(trackCenter + gap);
                    rightL.setEndX(trackCenter + gap + tickLength);
                    rightL.setStartY(py); rightL.setEndY(py);
                } else {
                    double px = x + pos;
                    rightL.setStartY(trackCenter + gap);
                    rightL.setEndY(trackCenter + gap + tickLength);
                    rightL.setStartX(px); rightL.setEndX(px);
                }
                if (majorIndex < leftTickLabels.size()) {
                    Text leftText = leftTickLabels.get(majorIndex);
                    boolean isExtreme = (v == firstMajor || Math.abs(v - max) < 0.001);
                    boolean visible = showLabels || isExtreme || isZero;
                    leftText.setVisible(visible);
                    if (visible) {
                        leftText.setStyle(String.format(java.util.Locale.US, "-fx-font-size: %.1fpx; -fx-fill: %s; -fx-font-weight: bold;", fontSize, color));
                        leftText.applyCss();
                        double twL = leftText.getLayoutBounds().getWidth();
                        double th = leftText.getLayoutBounds().getHeight();
                        double textGap = fontSize * 0.3;
                        if (isVert) {
                            double lx = trackCenter - gap - textGap - twL;
                            lx = Math.max(x + 2, lx);
                            leftText.setLayoutX(lx);
                            leftText.setLayoutY(y + pos);
                        } else {
                            leftText.setLayoutX(x + pos - twL / 2.0);
                            leftText.setLayoutY(trackCenter - gap - textGap - th / 2.0);
                        }
                    }
                }
                majorIndex++;
            }
            if (v < max && minorCount > 0) {
                double minorUnitVal = majorUnit / (minorCount + 1);
                for (int i = 1; i <= minorCount; i++) {
                    if (minorIndex < rightMinorTicks.size()) {
                        Line rightM = rightMinorTicks.get(minorIndex);
                        rightM.setVisible(showMinor);
                        if (showMinor) {
                            rightM.setStyle(String.format(java.util.Locale.US, "-fx-stroke: #666666; -fx-stroke-width: %.1fpx;", minorStroke));
                            double minorVal = v + minorUnitVal * i;
                            double mPerc = range > 0 ? (minorVal - min) / range : 0;
                            mPerc = Math.max(0, Math.min(1, mPerc));
                            double mPos = startOffset + (isVert ? (1.0 - mPerc) : mPerc) * usableTrack;
                            if (isVert) {
                                double py = y + mPos;
                                rightM.setStartX(trackCenter + gap);
                                rightM.setEndX(trackCenter + gap + minorLength);
                                rightM.setStartY(py); rightM.setEndY(py);
                            } else {
                                double px = x + mPos;
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
            return (h > 0 ? h : 300) * 0.40;
        }
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation() == Orientation.HORIZONTAL) {
            double w = width != -1 ? width : getSkinnable().getWidth();
            return (w > 0 ? w : 300) * 0.40;
        }
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }
    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) { return 0; }
    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) { return 0; }
}
