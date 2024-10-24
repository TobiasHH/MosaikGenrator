package de.tobiashh.javafx;

import de.tobiashh.javafx.model.MosaicImageModelImpl;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

// TODO Alle Logs mit {} Notation
// TODO Interface Javadoc ergänzen
// TODO Tooltips
// TODO Miniatur Übersicht als extra Fenster oder in der Ecke
// TODO ModelViewController besser umsetzen, siehe Sudoku Solver
// TODO EventHandler code aufräumen, da dafür Controller Sachen public gemacht wurden statt methoden genutzt
// TODO Scene Graph und Node ändernde Sachen nur auf Application Thread im Notfall mit Platform run later (prüfen ob so ist) andere Sachen auslagern
// TODO Bei neuberechnung kann man über leere Fenster hovern und sieht unten Infos
// Todo Exceptions nachgehen
// TODO Workflow geradeziehen z.B. stoßen Listener gerade den Recalculate mehrfach an (tilesinRow ändern)
// TODO Werden unten alle Infos mit Platform gesetzt?
// TODO Schaltflächen Sperren solange berechnet wird (Neuklick verhindern)
// TODO DEbug Infos im Tile speichern sodass man beim REplace ignore nur die geänderten TileViews neu laden muss mit setTiles
// TODO Bildpfad vom SRC IMage in UI ausgeben
// TODO Progress rs für die einzelnen Schritte wie https://stackoverflow.com/questions/37087848/task-progress-bar-javafx-application

/**
 * JavaFX App
 */
public class App extends Application {
    private final static Logger LOGGER = LoggerFactory.getLogger(App.class.getName());

    private static Parent loadFXML() throws IOException {
        LOGGER.info("loadFXML");
        URL resource = Controller.class.getResource("primary.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setControllerFactory(c -> new Controller(new MosaicImageModelImpl()));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("start");
        Scene scene = new Scene(loadFXML(), 1280, 960);
        stage.setScene(scene);
        stage.show();
    }

}