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
		Tracks t1 = new Tracks("C:\\Users\\Pietro\\Downloads\\Someone You Loved.wav");
		Deck d1 = new Deck();
		d1.loadTrack(t1);

		consoleController.setDeck(d1);
		//consoleController.initialize();
	}
}
