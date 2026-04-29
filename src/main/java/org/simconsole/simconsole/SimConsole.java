package org.simconsole.simconsole;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SimConsole extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(SimConsole.class.getResource("console-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), (int)(1920/3), (int)(1080/3));
		ConsoleController consoleController = fxmlLoader.getController();

		stage.setTitle("SimConsole");
		//stage.resizableProperty().setValue(false);
		stage.setMinHeight((int)(1080/6));
		stage.setMinWidth((int)(1920/6));
		stage.setScene(scene);
		stage.show();


		Tracks trackA = new Tracks("C:\\Users\\Pietro\\Downloads\\Queen – Bohemian Rhapsody (Official Video Remastered).wav");
		Tracks trackB = new Tracks("C:\\Users\\Pietro\\Downloads\\Lacrimosa.wav");
		Deck d1 = new Deck();
		d1.loadTrack(trackA);
		System.out.println("Test: Traccia A partita.");


		d1.setVolume(1);
		d1.setEqHigh(-12);
		d1.setEqMid(0);
		d1.setEqLow(12);


		consoleController.setDeck(d1);
	}
}
