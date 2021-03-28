package de.tobiashh.javafx;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Arrays;

public class Controller {
    private final MosaikImageModel model;

    @FXML public MenuBar menuBar;

    @FXML public Canvas canvas;

    @FXML public ScrollPane scrollPane;

    @FXML public Label cursorPositionLabel;

    @FXML public Label fileLabel;

    @FXML public Label pathLabel;

    @FXML public Label filesCountLabel;

    @FXML public Label tileHoverLabel;

    @FXML public Pane canvasPane;

    @FXML public CheckBox originalCheck;

    private static final double SCALE_DEFAULT = 1.0;
    private static final double SCALE_MIN = 0.1;
    private static final double SCALE_MAX = 2;

    private final DoubleProperty scale = new SimpleDoubleProperty(SCALE_DEFAULT);

    private final ObjectProperty<Path> imagePath = new SimpleObjectProperty<>();

    private final BooleanProperty displayOriginalImage = new SimpleBooleanProperty();

    public Controller(MosaikImageModel model) {
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
        pathLabel.textProperty().bind(Bindings.when(model.mosaikTilesPathProperty().isNull()).then("Kein Pfad gewählt.").otherwise(model.mosaikTilesPathProperty().asString()));
        filesCountLabel.textProperty().bind(model.dstTilesCountProperty().asString());

        canvas.widthProperty().bind(canvasPane.prefWidthProperty());
        canvas.heightProperty().bind(canvasPane.prefHeightProperty());
    }

    private void initEventHandler() {
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, getCursorPositionEventHandler());
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, getTileHoverEventHandler());
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, changeTileEventHandler());
        scrollPane.addEventFilter(ScrollEvent.SCROLL,getScrollEventHandler());
    }

    private EventHandler<MouseEvent>  changeTileEventHandler() {
        return mouseEvent -> {
            int x = (int) (mouseEvent.getX() / (model.getTileSize() * getScale()));
            int y = (int) (mouseEvent.getY() / (model.getTileSize() * getScale()));
            model.deleteTile(x,y);
        };
    }

    private EventHandler<MouseEvent> getCursorPositionEventHandler() {
        return mouseEvent -> cursorPositionLabel.setText("x:" + (int) mouseEvent.getX() + " y:" + (int) mouseEvent.getY());
    }

    private EventHandler<MouseEvent> getTileHoverEventHandler() {
        return mouseEvent -> tileHoverLabel.setText("x:" + (int) (mouseEvent.getX() / (model.getTileSize() * getScale())) + " y:" + (int) (mouseEvent.getY() / (model.getTileSize()  * getScale())));
    }


    private void initChangeListener() {
        scale.addListener((observable, oldValue, newValue) -> drawImage());
        model.compositeImageProperty().addListener((observable, oldImage, newImage) -> drawImage());
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
        try {
            String filename = "test.png";
            model.setImageFile(Path.of(getClass().getResource(filename).toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void scaleCanvasPane(BufferedImage image) {
        System.out.println("Controller.scaleCanvas");
        canvasPane.setPrefWidth((int)(image.getWidth() * getScale()));
        canvasPane.setPrefHeight((int)(image.getHeight() * getScale()));
    }

    private void drawImage() {
        System.out.println("Controller.drawImage");
        BufferedImage bufferedImage = model.getCompositeImage();
        scaleCanvasPane(bufferedImage);

        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        gc.drawImage(image, 0,0, canvas.getWidth(),canvas.getHeight());
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
        if(getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().toFile());
        fileChooser.setTitle("Öffne Bild");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(MosaikImageModelImpl.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        Path image = fileChooser.showOpenDialog(menuBar.getScene().getWindow()).toPath();
        model.setImageFile(image);
        setImagePath(image.getParent());
    }

    public void processTilesPath(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wähle Mosaik Tile Pfad");
        directoryChooser.setInitialDirectory(model.getMosaikTilesPath().toFile());
        model.setMosaikTilesPath(directoryChooser.showDialog(menuBar.getScene().getWindow()).toPath());
    }

    public DoubleProperty scaleProperty() { return scale; }

    public double getScale() { return scale.get(); }

    public void setScale(double scale) { this.scale.set(Math.max(Math.min(scale, SCALE_MAX), SCALE_MIN)); }

    public ObjectProperty<Path>  imagePathProperty(){ return imagePath; }

    public Path getImagePath() { return imagePath.get(); }

    public void setImagePath(Path imagePath) { this.imagePath.set(imagePath); }

    public BooleanProperty displayOriginalImageProperty(){ return displayOriginalImage; }

    public boolean getDisplayOriginalImage() { return displayOriginalImage.get(); }

    public void setDisplayOriginalImage(boolean displayOriginalImage) { this.displayOriginalImage.set(displayOriginalImage); }

    public void dragDropped(DragEvent dragEvent) {
        System.out.println("Controller.dragDropped");
        Dragboard dragboard = dragEvent.getDragboard();
        boolean success = false;
        if (dragboard.hasFiles()) {
            if(hasDirectory(dragboard))
            {
                File folder = dragboard.getFiles().get(0);
                model.setMosaikTilesPath(folder.toPath());
                success = true;
            }
            else if (hasImageFile(dragboard))
            {
                Path path = dragboard.getFiles().get(0).toPath();
                if(isImage(path))
                {
                    model.setImageFile(path);
                }
            }
        }

        dragEvent.setDropCompleted(success);

        dragEvent.consume();
    }

    public void dragOver(DragEvent dragEvent) {
        System.out.println("Controller.dragOver");
        Dragboard dragboard = dragEvent.getDragboard();
        if(dragEvent.getGestureSource() != scrollPane){

            if(dragboard.hasFiles())
            {
                System.out.println(hasDirectory(dragboard));
                System.out.println(hasImageFile(dragboard));

                if(hasDirectory(dragboard) || hasImageFile(dragboard))
                {
                    dragEvent.acceptTransferModes(TransferMode.ANY);
                }
            }
            else {
                dragEvent.consume();
            }
        }
    }

    private boolean hasDirectory(Dragboard dragboard) {
        return dragboard.getFiles().size() == 1 && dragboard.getFiles().get(0).isDirectory();
    }

    private boolean hasImageFile(Dragboard dragboard) {
        return dragboard.getFiles().size() == 1 && isImage(dragboard.getFiles().get(0).toPath());
    }

    private boolean isImage(Path path) {
        return Arrays.stream(MosaikImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())));
    }

    public void originalCheckAction(ActionEvent actionEvent) {
        displayOriginalImage.set(originalCheck.isSelected());
    }

    public void reloadImage(ActionEvent actionEvent) {
        drawImage();
    }

    public void recalculateImage(ActionEvent actionEvent) {
        model.calculateMosaik();
     //   drawImage();
    }
}
