package org.simconsole.simconsole;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.layout.Region;

public class DynamicSlider extends Slider {

    // Track style parts — kept separate for clean logic, combined in applyTrackStyle()
    private String trackGradientStyle = "";
    private String trackRadiusStyle   = "";

    public DynamicSlider() {
        super();
        initDynamicStyle();
    }

    public DynamicSlider(double min, double max, double value) {
        super(min, max, value);
        initDynamicStyle();
    }

    private void initDynamicStyle() {
        this.skinProperty().addListener((obs, old, skin) -> {
            if (skin == null) return;

            Node track = this.lookup(".track");
            Node thumb = this.lookup(".thumb");
            if (track == null || thumb == null) return;

            initResponsive(track, thumb);

            ChangeListener<Object> listener = (o, ov, nv) -> updateSliderTrack(track, thumb);
            track.layoutBoundsProperty().addListener(listener);
            thumb.layoutBoundsProperty().addListener(listener);
            this.valueProperty().addListener(listener);

            updateSliderTrack(track, thumb);
        });
    }

    // Combines the two track style parts and applies them in a single setStyle() call
    private void applyTrackStyle(Node track) {
        track.setStyle(trackGradientStyle + trackRadiusStyle);
    }

    private void initResponsive(Node track, Node thumb) {
        if (!(track instanceof Region trackRegion) || !(thumb instanceof Region thumbRegion)) return;

        if (this.getOrientation() == Orientation.VERTICAL) {
            this.maxWidthProperty().bind(this.heightProperty().multiply(0.15));
            trackRegion.prefWidthProperty().bind(this.widthProperty().multiply(0.3));
            thumbRegion.prefWidthProperty().bind(this.widthProperty().multiply(0.9));
            thumbRegion.prefHeightProperty().bind(this.widthProperty().multiply(0.9));
        } else {
            this.maxHeightProperty().bind(this.widthProperty().multiply(0.15));
            trackRegion.prefHeightProperty().bind(this.heightProperty().multiply(0.3));
            thumbRegion.prefWidthProperty().bind(this.heightProperty().multiply(0.9));
            thumbRegion.prefHeightProperty().bind(this.heightProperty().multiply(0.9));
        }

        // Track radius: separate listener, updates its own part and re-applies combined style
        track.layoutBoundsProperty().addListener((obs, old, bounds) -> {
            double shortSide = Math.min(bounds.getWidth(), bounds.getHeight());
            double radius = shortSide / 2.0;
            trackRadiusStyle = String.format(java.util.Locale.US,
                    "-fx-background-radius: %.1fpx; -fx-border-radius: %.1fpx;",
                    radius, radius);
            applyTrackStyle(track);
        });

        // Thumb geometry: radius + border-width, colors stay in CSS (hover/pressed preserved)
        thumb.layoutBoundsProperty().addListener((obs, old, bounds) -> {
            double size = Math.min(bounds.getWidth(), bounds.getHeight());
            double r = size / 2.0;
            double borderWidth = size * 0.08;
            thumb.setStyle(String.format(java.util.Locale.US,
                    "-fx-background-radius: %.1fpx;" +
                    "-fx-border-radius: %.1fpx;" +
                    "-fx-border-width: %.1fpx;",
                    r, r, borderWidth));
        });
    }

    private void updateSliderTrack(Node track, Node thumb) {
        double min = this.getMin();
        double max = this.getMax();
        double val = this.getValue();
        double percentage = (max == min) ? 0 : (val - min) / (max - min);

        String colorFilled = "-track-filled";
        String colorEmpty = "-track-empty";

        if (this.getOrientation() == Orientation.VERTICAL) {
            double trackHeight = track.getLayoutBounds().getHeight();
            double thumbHeight = thumb.getLayoutBounds().getHeight();
            if (trackHeight > 0 && thumbHeight > 0) {
                double thumbRadius = thumbHeight / 2.0;
                double usableTrack = trackHeight - thumbHeight;
                double centerFromTop = thumbRadius + (1.0 - percentage) * usableTrack;
                double stopPercentage = (centerFromTop / trackHeight) * 100.0;

                trackGradientStyle = String.format(java.util.Locale.US,
                        "-fx-background-color: linear-gradient(to bottom, %s %.1f%%, %s %.1f%%);",
                        colorEmpty, stopPercentage, colorFilled, stopPercentage);
                applyTrackStyle(track);
            }
        } else {
            double trackWidth = track.getLayoutBounds().getWidth();
            double thumbWidth = thumb.getLayoutBounds().getWidth();
            if (trackWidth > 0 && thumbWidth > 0) {
                double thumbRadius = thumbWidth / 2.0;
                double usableTrack = trackWidth - thumbWidth;
                double centerFromLeft = thumbRadius + percentage * usableTrack;
                double stopPercentage = (centerFromLeft / trackWidth) * 100.0;

                trackGradientStyle = String.format(java.util.Locale.US,
                        "-fx-background-color: linear-gradient(to right, %s %.1f%%, %s %.1f%%);",
                        colorFilled, stopPercentage, colorEmpty, stopPercentage);
                applyTrackStyle(track);
            }
        }
    }
}
