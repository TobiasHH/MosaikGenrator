module de.tobiashh.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
   // requires java.desktop;

    opens de.tobiashh.javafx to javafx.fxml;
    exports de.tobiashh.javafx;
    exports de.tobiashh.javafx.tiles;
    exports de.tobiashh.javafx.properties;
    exports de.tobiashh.javafx.tools;
    exports de.tobiashh.javafx.compareable;
}