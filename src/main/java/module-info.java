module org.simconsole.simconsole {
	requires javafx.controls;
	requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;
    requires java.net.http;

    opens org.simconsole.simconsole to javafx.fxml;
	exports org.simconsole.simconsole;
}