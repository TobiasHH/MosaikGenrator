package de.tobiashh.javafx;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.MalformedURLException;

public class Controller {
    private final Model model;

    @FXML
    public MenuBar menuBar;

    @FXML
    public Canvas canvas;

    @FXML
    public Label cursorPositionLabel;

    @FXML
    public Label fileLabel;

    @FXML
    public ScrollPane scrollPane;

    public Controller(Model model) {
        this.model = model;
    }

    @FXML
    public void initialize() {
        canvas.scaleXProperty().bind(model.scaleProperty());
        canvas.scaleYProperty().bind(model.scaleProperty());

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, getDotEventHandler());
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, getCursorPositionEventHandler());
        scrollPane.addEventFilter(ScrollEvent.SCROLL,getScrollEventHandler());

        initCanvas();
    }

    private EventHandler<ScrollEvent> getScrollEventHandler() {
        return scrollEvent -> {
            if(scrollEvent.isControlDown()){
                model.setScale(model.getScale() + scrollEvent.getDeltaY());
                scrollEvent.consume();
            }
        };
    }

    private void initCanvas() {
        model.setImage( new Image(getClass().getResourceAsStream("test.png")));
        drawImage(model.getImage());
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
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.gif"));
        File selectedFile = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (selectedFile != null) {
            fileLabel.setText(selectedFile.getName());
            GraphicsContext gc = canvas.getGraphicsContext2D();

            try {
                Image image = new Image(String.valueOf(selectedFile.toURI().toURL()));
                canvas.setWidth(image.getWidth());
                canvas.setHeight(image.getHeight());
                gc.drawImage(image, 0,0, image.getWidth(), image.getHeight());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                fileLabel.setText("Bild nicht geladen!");
            }
        }
    }
}
