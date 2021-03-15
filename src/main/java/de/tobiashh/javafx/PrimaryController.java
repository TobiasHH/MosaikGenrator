package de.tobiashh.javafx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
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
        System.out.println("canvas");
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.RED);

        Image image = new Image(getClass().getResourceAsStream("test.png"));
        System.out.println("x");
        canvas.setWidth(image.getWidth());
        canvas.setHeight(image.getHeight());
        gc.drawImage(image, 0,0, image.getWidth(), image.getHeight());
        gc.setFont(new Font("", 100));
        gc.fillText("I LOVE YOU!", 75, 75);
        gc.fillOval(200,150,200,200);
        gc.fillOval(400,150,200,200);
        double[] x = {230,570,400};
        double[] y = {320,320,500};
        gc.fillPolygon(x,y,3);


        gc.fillRect(250,250,300,75);
        WritableImage s = canvas.snapshot(null, null);

        try {
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(s, null);
            File file = new File("out.png");
            ImageIO.write(bufferedImage, "png", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("y");
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
