package org.simconsole.simconsole;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("console-view.fxml"));
		Scene scene = new Scene(fxmlLoader.load(), (int)(1920/3), (int)(1080/3));
		stage.setTitle("SimConsole");
		stage.resizableProperty().setValue(false);
		stage.setScene(scene);
		stage.show();
	}
}
