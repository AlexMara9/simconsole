module org.simconsole.simconsole {
	requires javafx.controls;
	requires javafx.fxml;


	opens org.simconsole.simconsole to javafx.fxml;
	exports org.simconsole.simconsole;
}