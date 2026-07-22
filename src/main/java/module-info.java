module org.simconsole.simconsole {
	requires javafx.controls;
	requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires java.net.http;

    opens org.simconsole.simconsole to javafx.fxml;
    opens org.simconsole.simconsole.controllers to javafx.fxml;
    opens org.simconsole.simconsole.components to javafx.fxml;
    opens org.simconsole.simconsole.models to javafx.fxml;

	exports org.simconsole.simconsole;
	exports org.simconsole.simconsole.controllers;
	exports org.simconsole.simconsole.components;
	exports org.simconsole.simconsole.models;
}