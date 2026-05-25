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

		Tracks trackA = new Tracks("C:\\Users\\Pietro\\Downloads\\bassi.wav");
		Tracks trackB = new Tracks("C:\\Users\\Pietro\\Downloads\\mixaggio.wav");
		Tracks trackC = new Tracks("C:\\Users\\Pietro\\Downloads\\LosingIt.wav");
		Tracks trackD = new Tracks("C:\\Users\\Pietro\\Downloads\\Nightcrawler.wav");

		
		//t1
		Deck d1 = new Deck();
		DeckControls c1 = new DeckControls();
		d1.setControls(c1);

		AudioProcessor processor1 = new AudioProcessor(d1);
		processor1.startPlayback();

		//t2
		Deck d2 = new Deck();
		DeckControls c2 = new DeckControls();
		d2.setControls(c2);

		AudioProcessor processor2 = new AudioProcessor(d2);
		processor2.startPlayback();

		d1.loadTrack(trackD);
		d2.loadTrack(trackC);
		
		consoleController.setupDecks(d1, c1, d2, c2);
	}
}
