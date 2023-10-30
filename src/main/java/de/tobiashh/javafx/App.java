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

// TODO Mouse wird bei sehr klein gezoomten Bild nicht korrekt berechnet. klick wird auf faqlsche bild bei klick auf rand angewqendet
// TODO fehler wenn tile size 512
// TODO pre color allignment
// TODO performa3nce bei vielen Bildern

/**
 * JavaFX App
 */
public class App extends Application {
    private final static Logger LOGGER = LoggerFactory.getLogger(App.class.getName());

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("start");
        Scene scene = new Scene(loadFXML(), 640, 480);
        stage.setScene(scene);
   //     stage.setMaximized(true);
        stage.show();
    }

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

}