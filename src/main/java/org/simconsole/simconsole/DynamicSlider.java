package org.simconsole.simconsole;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Slider;

public class DynamicSlider extends Slider {

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

            ChangeListener<Object> listener = (o, ov, nv) -> updateSliderTrack(track, thumb);
            track.layoutBoundsProperty().addListener(listener);
            thumb.layoutBoundsProperty().addListener(listener);
            this.valueProperty().addListener(listener);

            updateSliderTrack(track, thumb);
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

                String style = String.format(
                        java.util.Locale.US,
                        "-fx-background-color: linear-gradient(to bottom, %s %.1f%%, %s %.1f%%);",
                        colorEmpty, stopPercentage, colorFilled, stopPercentage
                );
                track.setStyle(style);
            }
        } else {
            double trackWidth = track.getLayoutBounds().getWidth();
            double thumbWidth = thumb.getLayoutBounds().getWidth();
            if (trackWidth > 0 && thumbWidth > 0) {
                double thumbRadius = thumbWidth / 2.0;
                double usableTrack = trackWidth - thumbWidth;
                double centerFromLeft = thumbRadius + percentage * usableTrack;
                double stopPercentage = (centerFromLeft / trackWidth) * 100.0;

                String style = String.format(
                        java.util.Locale.US,
                        "-fx-background-color: linear-gradient(to right, %s %.1f%%, %s %.1f%%);",
                        colorFilled, stopPercentage, colorEmpty, stopPercentage
                );
                track.setStyle(style);
            }
        }
    }
}
