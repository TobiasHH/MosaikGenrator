package de.tobiashh.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * JavaFX App
 */
public class App extends Application {
    private final static Logger LOGGER = Logger.getLogger(App.class.getName());

    @Override
    public void start(Stage stage) throws IOException {
        LOGGER.info("App.start");
        Scene scene = new Scene(loadFXML(), 640, 480);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private static Parent loadFXML() throws IOException {
        LOGGER.info("App.loadFXML");
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("primary.fxml"));
        MosaicImageModel model = new MosaicImageModelImpl();
        fxmlLoader.setControllerFactory(c -> new Controller(model));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}