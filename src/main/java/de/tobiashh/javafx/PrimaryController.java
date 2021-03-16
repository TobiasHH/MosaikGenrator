package de.tobiashh.javafx;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PrimaryController {

    @FXML
    public MenuBar menuBar;

    @FXML
    public Canvas canvas;

    @FXML
    public void initialize() {
        canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            System.out.println("mouse click detected! " + mouseEvent.getSource());
            System.out.println("x: " + mouseEvent.getX());
            System.out.println("y: " + mouseEvent.getY());
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.fillOval(mouseEvent.getX() - 5, mouseEvent.getY() - 5, 10,10);
        });

        GraphicsContext gc = canvas.getGraphicsContext2D();

        Image image = new Image(getClass().getResourceAsStream("test.png"));
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
        gc.drawImage(image, 0,0, image.getWidth(), image.getHeight());
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
}
