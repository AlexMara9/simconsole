package org.simconsole.simconsole;
import org.simconsole.simconsole.models.AudioProcessor;
import org.simconsole.simconsole.controllers.ConsoleController;
import org.simconsole.simconsole.models.DeckControls;
import org.simconsole.simconsole.models.Deck;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
public class SimConsole extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(SimConsole.class.getResource("fxml/console-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), (int)(1920/3), (int)(1080/3));
		ConsoleController consoleController = fxmlLoader.getController();
		stage.setTitle("SimConsole");
		stage.setMinHeight((int)(1080/6));
		stage.setMinWidth((int)(1920/6));
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.setOnCloseRequest(e -> {
			javafx.application.Platform.exit();
			System.exit(0);
		});
		stage.show();
		Deck d1 = new Deck();
		DeckControls c1 = new DeckControls();
		d1.setControls(c1);
		AudioProcessor processor1 = new AudioProcessor(d1);
		processor1.startPlayback();
		Deck d2 = new Deck();
		DeckControls c2 = new DeckControls();
		d2.setControls(c2);
		AudioProcessor processor2 = new AudioProcessor(d2);
		processor2.startPlayback();
        consoleController.setDecks(d1, d2);
	}
}
