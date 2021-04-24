module de.tobiashh.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;
    requires org.slf4j;

    opens de.tobiashh.javafx to javafx.fxml;
    exports de.tobiashh.javafx;
    exports de.tobiashh.javafx.tiles;
    exports de.tobiashh.javafx.model;
    exports de.tobiashh.javafx.tools;
    exports de.tobiashh.javafx.compareable;
}