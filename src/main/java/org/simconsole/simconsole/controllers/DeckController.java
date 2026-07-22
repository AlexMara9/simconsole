package org.simconsole.simconsole.controllers;
import org.simconsole.simconsole.models.Deck;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
/**
 * Deck UI controller
 */
public class DeckController {
	private static final double VINYL_SIZE_FACTOR = 0.45;
	private static final double CENTER_SIZE_FACTOR = 0.2;
	private static final double CENTER_STROKE_FACTOR = 0.04;
	private static final double ROTATION_SPEED_FACTOR = 0.3;
	private double oldMouseAngle;
	@FXML private StackPane deckContainer;
	@FXML private Circle vinyl;
	@FXML private Circle vinylCenter;
	@FXML
	public void initialize() {
		responsive();
	}
	private void responsive(){
		DoubleBinding minDim = Bindings.createDoubleBinding(
				() -> Math.min(deckContainer.getWidth(), deckContainer.getHeight()),
				deckContainer.widthProperty(),
				deckContainer.heightProperty()
		);
		vinyl.radiusProperty().bind(minDim.multiply(VINYL_SIZE_FACTOR));
		vinylCenter.radiusProperty().bind(vinyl.radiusProperty().multiply(CENTER_SIZE_FACTOR));
		vinylCenter.strokeWidthProperty().bind(vinyl.radiusProperty().multiply(CENTER_STROKE_FACTOR));
	}
	@FXML
	private void onScroll(ScrollEvent event){
		double delta = event.getDeltaY();
		vinyl.setRotate(vinyl.getRotate() + delta * ROTATION_SPEED_FACTOR);
	}
	@FXML
	private void onMousePressed(MouseEvent event){
		oldMouseAngle = mouseAngle(event.getX(), event.getY());
	}
	@FXML
	private void onMouseMoved(MouseEvent event){
		double angle = mouseAngle(event.getX(), event.getY());
		double delta = angle - oldMouseAngle;
		vinyl.setRotate(vinyl.getRotate() + delta);
		oldMouseAngle = angle;
	}
	@FXML
	private void onTouchPressed(TouchEvent event){
		oldMouseAngle = mouseAngle(event.getTouchPoint().getX(), event.getTouchPoint().getY());
	}
	@FXML
	private void onTouchMoved(TouchEvent event){
		double angle = mouseAngle(event.getTouchPoint().getX(), event.getTouchPoint().getY());
		double delta = angle - oldMouseAngle;
		vinyl.setRotate(vinyl.getRotate() + delta);
		oldMouseAngle = angle;
	}
	private double mouseAngle(Double x, Double y){
		double centerX = deckContainer.getWidth() / 2;
		double centerY = deckContainer.getHeight() / 2;
		return Math.toDegrees(Math.atan2(y - centerY,x - centerX));
	}
}
