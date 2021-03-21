package de.tobiashh.javafx;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;

public class Controller {
    public static final String KEIN_PFAD_AUSGEWAEHLT = "Kein Pfad ausgewählt!";

    private final MosaikImageModel model;

    @FXML
    public MenuBar menuBar;

    @FXML
    public Canvas canvas;

    @FXML
    public ScrollPane scrollPane;

    @FXML
    public Label cursorPositionLabel;

    @FXML
    public Label fileLabel;

    @FXML
    public Label pathLabel;

    @FXML
    public Label tilesCountLabel;

    private static final String SCALE_PROPERTY_KEY = "scale";
    private static final double SCALE_DEFAULT = 1.0;
    private static final double SCALE_MIN = 0.1;
    private static final double SCALE_MAX = 2;

    private final DoubleProperty scale = new SimpleDoubleProperty(SCALE_DEFAULT);

    public Controller(CalculationModel model) {
        this.model = model;
    }

    @FXML
    public void initialize() {
        initChangeListener();
        initEventHandler();
        initBindings();
        initCanvas();
    }

    private void initBindings() {
        pathLabel.textProperty().bind(Bindings.when(model.tilesPathProperty().isNull()).then("Kein Pfad gewählt.").otherwise(model.tilesPathProperty().asString()));
        tilesCountLabel.textProperty().bind(model.tileCountProperty().asString());
    }

    private void initEventHandler() {
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, getDotEventHandler());
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, getCursorPositionEventHandler());
        scrollPane.addEventFilter(ScrollEvent.SCROLL,getScrollEventHandler());
    }

    private void initChangeListener() {
        scale.addListener((observable, oldValue, newValue) -> {
            double scaledWidth = model.getImage().getWidth() * newValue.doubleValue();
            double scaledHeight = model.getImage().getHeight() * newValue.doubleValue();
            canvas.setWidth(scaledWidth);
            canvas.setHeight(scaledHeight);
        });

        model.imageProperty().addListener((observable, oldImage, newImage) -> drawImage(newImage));
    }

    private EventHandler<ScrollEvent> getScrollEventHandler() {
        return scrollEvent -> {
            if(scrollEvent.isControlDown()){
                setScale(getScale() + scrollEvent.getDeltaY() / 100.0);
                scrollEvent.consume();
            }
        };
    }

    private void initCanvas() {
        model.setImageFile( new File(getClass().getResource("test.png").getFile()));
    }

    private void drawImage(Image image) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
        gc.drawImage(image, 0,0, image.getWidth(), image.getHeight());
    }

    private EventHandler<MouseEvent> getDotEventHandler() {
        return mouseEvent -> {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.fillOval(mouseEvent.getX() - 5, mouseEvent.getY() - 5, 10, 10);
        };
    }

    private EventHandler<MouseEvent> getCursorPositionEventHandler() {
        return mouseEvent -> cursorPositionLabel.setText("x:" + (int) mouseEvent.getX() + " y:" + (int) mouseEvent.getY());
    }

    public void processExit(ActionEvent actionEvent) {
        Platform.exit();
    }

    @FXML
    public void showAboutDialog(ActionEvent actionEvent) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(menuBar.getScene().getWindow());
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("About"));
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    public void processOpen(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Öffne Bild");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(CalculationModel.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        File selectedFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (selectedFile != null) {
            model.setImageFile(selectedFile);
        }
    }

    public void processTilesPath(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wähle Tile Pfad");
        File selectedFile = directoryChooser.showDialog(menuBar.getScene().getWindow());
        if (selectedFile != null) {
            model.setTilesPath(selectedFile);
        }
    }

    public DoubleProperty scaleProperty() { return scale; }

    public double getScale() { return scale.get(); }

    public void setScale(double scale) { this.scale.set(Math.max(Math.min(scale, SCALE_MAX), SCALE_MIN)); }
}
