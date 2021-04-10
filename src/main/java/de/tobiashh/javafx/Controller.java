package de.tobiashh.javafx;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
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
    private final MosaicImageModel model;

    @FXML public MenuBar menuBar;

    @FXML public Canvas canvas;

    @FXML public ScrollPane scrollPane;

    @FXML public Label cursorPositionLabel;

    @FXML public Label fileLabel;

    @FXML public Label pathLabel;

    @FXML public Label filesCountLabel;

    @FXML public Label tileHoverLabel;

    @FXML public Label tileImageInformations;

    @FXML public Pane canvasPane;

    @FXML public CheckBox originalCheck;

    @FXML public CheckBox linearModeCheck;

    @FXML public CheckBox scanSubfolderCheck;

    @FXML public CheckBox blurCheck;

    @FXML public TextField preColorAlignment;

    @FXML public TextField postColorAlignment;

    @FXML public TextField opacity;

    @FXML public TextField tilesPerRow;

    @FXML public TextField maxReuses;

    @FXML public TextField reuseDistance;

    private static final double SCALE_DEFAULT = 1.0;
    private static final double SCALE_MIN = 0.1;
    private static final double SCALE_MAX = 2;

    private final DoubleProperty scale = new SimpleDoubleProperty(SCALE_DEFAULT);

    private final ObjectProperty<Path> imagePath = new SimpleObjectProperty<>();

    private final BooleanProperty displayOriginalImage = new SimpleBooleanProperty();

    public Controller(MosaicImageModel model) {
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
        pathLabel.textProperty().bind(Bindings.when(model.mosaicTilesPathProperty().isNull()).then("Kein Pfad gewählt.").otherwise(model.mosaicTilesPathProperty().asString()));
        filesCountLabel.textProperty().bind(model.dstTilesCountProperty().asString());
        canvas.widthProperty().bind(canvasPane.prefWidthProperty());
        canvas.heightProperty().bind(canvasPane.prefHeightProperty());
        linearModeCheck.selectedProperty().bindBidirectional(model.linearModeProperty());
        scanSubfolderCheck.selectedProperty().bindBidirectional(model.scanSubFolderProperty());
        blurCheck.selectedProperty().bindBidirectional(model.blurModeProperty());
    }

    private void initEventHandler() {
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, getCursorPositionEventHandler());
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, getTileHoverEventHandler());
        canvas.addEventHandler(MouseEvent.MOUSE_MOVED, getTileImageInformationEventHandler());
        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, changeTileEventHandler());
        scrollPane.addEventFilter(ScrollEvent.SCROLL,getScrollEventHandler());
    }

    private EventHandler<MouseEvent> changeTileEventHandler() {
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
        return mouseEvent -> {
            int tileX = (int) (mouseEvent.getX() / (model.getTileSize() * getScale()));
            int tileY =(int)( mouseEvent.getY() / (model.getTileSize() * getScale()));
            tileHoverLabel.setText("x:" + tileX + " y:" + tileY);
        };
    }

    private EventHandler<MouseEvent> getTileImageInformationEventHandler() {
        return mouseEvent -> {
            int tileX = (int) (mouseEvent.getX() / (model.getTileSize() * getScale()));
            int tileY =(int)( mouseEvent.getY() / (model.getTileSize() * getScale()));
            tileImageInformations.setText(model.getMosaicTileInformation(tileX, tileY));
        };
    }


    private void initChangeListener() {
        scale.addListener((observable, oldValue, newValue) -> drawImage());
        model.compositeImageProperty().addListener((observable, oldImage, newImage) -> drawImage());

        preColorAlignment.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue,0,100);
            preColorAlignment.setText(String.valueOf(percent));
            model.setPreColorAlignment(percent);
        });
        model.preColorAlignmentProperty().addListener((observable, oldValue, newValue) -> preColorAlignment.setText(String.valueOf(newValue)));
        preColorAlignment.setText(String.valueOf(model.getPreColorAlignment()));

        postColorAlignment.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue,0,100);
            postColorAlignment.setText(String.valueOf(percent));
            model.setPostColorAlignment(percent);
        });
        model.postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> postColorAlignment.setText(String.valueOf(newValue)));
        postColorAlignment.setText(String.valueOf(model.getPostColorAlignment()));

        opacity.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue,0,100);
            opacity.setText(String.valueOf(percent));
            model.setOpacity(percent);
        });
        model.opacityProperty().addListener((observable, oldValue, newValue) -> opacity.setText(String.valueOf(newValue)));
        opacity.setText(String.valueOf(model.getOpacity()));

        tilesPerRow.textProperty().addListener((observable, oldValue, newValue) -> {
            int i = getIntFromString(newValue, 1, 50);
            tilesPerRow.setText(String.valueOf(i));
            model.setTilesPerRow(i);
        });
        model.tilesPerRowProperty().addListener((observable, oldValue, newValue) -> tilesPerRow.setText(String.valueOf(newValue)));
        tilesPerRow.setText(String.valueOf(model.getTilesPerRow()));

        maxReuses.textProperty().addListener((observable, oldValue, newValue) -> {

            int i = getIntFromString(newValue, 0, 5000);
            maxReuses.setText(String.valueOf(i));
            model.setMaxReuses(i);
        });
        model.maxReusesProperty().addListener((observable, oldValue, newValue) -> maxReuses.setText(String.valueOf(newValue)));
        maxReuses.setText(String.valueOf(model.getMaxReuses()));

        reuseDistance.textProperty().addListener((observable, oldValue, newValue) -> {
            int i = getIntFromString(newValue, 1, 50);
            reuseDistance.setText(String.valueOf(i));
            model.setReuseDistance(i);
        });
        model.reuseDistanceProperty().addListener((observable, oldValue, newValue) -> reuseDistance.setText(String.valueOf(newValue)));
        reuseDistance.setText(String.valueOf(model.getReuseDistance()));

        displayOriginalImage.addListener((observable, oldValue, newValue) -> drawImage());
    }

    private int getIntFromString(String value, int min, int max){
        String digitString = value.replaceAll("[^\\d]", "");
        digitString = digitString.substring(0,Math.min(5, digitString.length()));
        return Math.min(Math.max(min,Integer.parseInt("0" + digitString)),max);
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

    private void drawImage() {
        BufferedImage bufferedImage = (isDisplayOriginalImage())? model.getOriginalImage() : model.getCompositeImage();

        canvasPane.setPrefWidth((int)(bufferedImage.getWidth() * getScale()));
        canvasPane.setPrefHeight((int)(bufferedImage.getHeight() * getScale()));

        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        gc.drawImage(image, 0,0, canvas.getWidth(),canvas.getHeight());
    }

    @FXML public void processExit() {
        Platform.exit();
    }

    @FXML public void showAboutDialog() {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(menuBar.getScene().getWindow());
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("About"));
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    @FXML public void processOpen() {
        FileChooser fileChooser = new FileChooser();
        if(getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().toFile());
        fileChooser.setTitle("Bild öffnen");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if(file != null)
        {
            Path path = file.toPath();
            if(Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.endsWith("." + e))){
                model.setImageFile(path);
                setImagePath(path.getParent());
            }
        }
    }

     @FXML public void processTilesPath() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wähle Mosaik Tile Pfad");
        directoryChooser.setInitialDirectory(model.getMosaicTilesPath().toFile());
        model.setMosaicTilesPath(directoryChooser.showDialog(menuBar.getScene().getWindow()).toPath());
    }

    public double getScale() { return scale.get(); }

    public void setScale(double scale) { this.scale.set(Math.max(Math.min(scale, SCALE_MAX), SCALE_MIN)); }

    public Path getImagePath() { return imagePath.get(); }

    public void setImagePath(Path imagePath) { this.imagePath.set(imagePath); }

    public boolean isDisplayOriginalImage() { return displayOriginalImage.get(); }

    @FXML public void dragDropped(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        boolean success = false;
        if (dragboard.hasFiles()) {
            if(hasDirectory(dragboard))
            {
                File folder = dragboard.getFiles().get(0);
                model.setMosaicTilesPath(folder.toPath());
                success = true;
            }
            else if (hasImageFile(dragboard))
            {
                Path path = dragboard.getFiles().get(0).toPath();
                if(isImage(path))
                {
                    model.setImageFile(path);
                    setImagePath(path.getParent());
                }
            }
        }

        dragEvent.setDropCompleted(success);

        dragEvent.consume();
    }

    @FXML public void dragOver(DragEvent dragEvent) {
        Dragboard dragboard = dragEvent.getDragboard();
        if(dragEvent.getGestureSource() != scrollPane){

            if(dragboard.hasFiles())
            {

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
        return Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())));
    }

    @FXML public void recalculateImage() {
        model.calculateMosaicImage();
    }

    @FXML public void originalCheckAction() {
        displayOriginalImage.set(originalCheck.isSelected());
    }

    @FXML public void processSave() {
        FileChooser fileChooser = new FileChooser();
        if(getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().toFile());
        fileChooser.setTitle("Bild speichern");
        if(getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().toFile());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if(file != null)
        {
            Path path = file.toPath();
            if(Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.endsWith("." + e))){
                model.saveImage(path);
                setImagePath(path.getParent());
            }
        }
    }
}
