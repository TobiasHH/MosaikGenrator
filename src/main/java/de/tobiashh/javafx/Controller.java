package de.tobiashh.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller {
    private final static Logger LOGGER = LoggerFactory.getLogger(Controller.class.getName());
    private static final double SCALE_DEFAULT = 1.0;
    private static final double SCALE_MIN = 0.1;
    private static final double SCALE_MAX = 10.0;
    private final MosaicImageModel model;
    private final DoubleProperty scale = new SimpleDoubleProperty(SCALE_DEFAULT);
    private final ObjectProperty<Path> imagePath = new SimpleObjectProperty<>();
    private final BooleanProperty displayOriginalImage = new SimpleBooleanProperty();

    @FXML
    private MenuBar menuBar;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private Label cursorPositionLabel;
    @FXML
    private Label pathLabel;
    @FXML
    private Label filesCountLabel;
    @FXML
    private Label tileHoverLabel;
    @FXML
    private Label tileImageInformations;
    @FXML
    private Pane canvasPane;
    @FXML
    private CheckBox originalCheck;
    @FXML
    private CheckBox linearModeCheck;
    @FXML
    private CheckBox scanSubfolderCheck;
    @FXML
    private TextField preColorAlignment;
    @FXML
    private TextField postColorAlignment;
    @FXML
    private TextField opacity;
    @FXML
    private TextField tilesPerRow;
    @FXML
    private TextField maxReuses;
    @FXML
    private TextField reuseDistance;
    @FXML
    private Label statusLabel;

    public Controller(MosaicImageModel model) {
        LOGGER.info("Controller");
        this.model = model;
    }

    @FXML
    private void initialize() {
        LOGGER.info("initialize");
        initChangeListener();
        initEventHandler();
        initBindings();
        initCanvas();
    }

    private void initBindings() {
        LOGGER.info("initBindings");
        pathLabel.textProperty().bind(Bindings.when(model.dstTilesPathProperty().isNull()).then("Kein Pfad gewählt.").otherwise(model.dstTilesPathProperty().asString()));
        filesCountLabel.textProperty().bind(model.dstTilesCountProperty().asString());
        linearModeCheck.selectedProperty().bindBidirectional(model.linearModeProperty());
        scanSubfolderCheck.selectedProperty().bindBidirectional(model.scanSubFolderProperty());
        statusLabel.textProperty().bind(model.statusProperty());
    }

    // todo Wird noch interessant beim laden von bilder nach opacity und co () nur visible updaten
    private void setTileVisibility() {
        System.out.println("Controller.setTileVisibility");
        double hmin = scrollPane.getHmin();
        double hmax = scrollPane.getHmax();
        double hvalue = scrollPane.getHvalue();
        double contentWidth = canvasPane.getLayoutBounds().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();

        double hoffset = Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);

        double vmin = scrollPane.getVmin();
        double vmax = scrollPane.getVmax();
        double vvalue = scrollPane.getVvalue();
        double contentHeight = canvasPane.getLayoutBounds().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double voffset = Math.max(0, contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);

        tiles.forEach(tileView -> {
            //        tileView.setTileVisible(tileView.intersects(hoffset, voffset, viewportWidth, viewportHeight));
        });
    }


    private void scaleTiles() {
        System.out.println("Controller.scaleTiles");

        int tileSize = (int)( model.getTileSize() * getScale());

        tiles.forEach(tileView -> tileView.setTileSize( tileSize));

        canvasPane.setPrefWidth(tileSize * model.getTilesPerRow());
        canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
    }

    private void setTiles() {
        System.out.println("Controller.setTiles");
        tiles.forEach(tileView -> {
            int x = tileView.getTilePositionX();
            int y = tileView.getTilePositionY();

            tileView.setTile((isDisplayOriginalImage()) ? model.getOriginalTile(x, y) : model.getTile(x, y));
        });
    }

    List<TileView> tiles = new ArrayList<>();

    private void initTileViews() {
        System.out.println("Controller.initTileViews");
        int tilesPerRow = model.getTilesPerRow();
        int tilesPerColumn = model.getTilesPerColumn();

        tiles.clear();

        int tileSize = (int) (getScale() * model.getTileSize());

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {

                TileView tileView = new TileView(x, y, tileSize);
                tileView.setFitWidth(tileSize);
                tileView.setFitHeight(tileSize);

                tiles.add(tileView);
            }
        }

        if(Platform.isFxApplicationThread()){
            canvasPane.getChildren().clear();
            canvasPane.getChildren().addAll(tiles);
            canvasPane.setPrefWidth(tileSize * model.getTilesPerRow());
            canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
        }
        else
        {
            Platform.runLater(() -> {
                canvasPane.getChildren().clear();
                canvasPane.getChildren().addAll(tiles);
                canvasPane.setPrefWidth(tileSize * model.getTilesPerRow());
                canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
            });
        }
    }

    private void initEventHandler() {
        LOGGER.info("initEventHandler");
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, getCursorPositionEventHandler());
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, getTileHoverEventHandler());
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, getTileImageInformationEventHandler());
        canvasPane.addEventHandler(MouseEvent.MOUSE_CLICKED, changeTileEventHandler());
        scrollPane.addEventFilter(ScrollEvent.SCROLL, getScrollEventHandler());
    }

    private EventHandler<MouseEvent> changeTileEventHandler() {
        LOGGER.info("changeTileEventHandler");
        return mouseEvent -> {
            int x = (int) (mouseEvent.getX() / (model.getTileSize() * getScale()));
            int y = (int) (mouseEvent.getY() / (model.getTileSize() * getScale()));
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                model.addAreaOfIntrest(x, y);
            }

            if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                model.removeAreaOfIntrest(x, y);
            }
        };
    }

    private EventHandler<MouseEvent> getCursorPositionEventHandler() {
        LOGGER.info("getCursorPositionEventHandler");
        return mouseEvent -> cursorPositionLabel.setText("x:" + (int) mouseEvent.getX() + " y:" + (int) mouseEvent.getY());
    }

    private EventHandler<MouseEvent> getTileHoverEventHandler() {
        LOGGER.info("getTileHoverEventHandler");
        return mouseEvent -> {
            int tileX = (int) (mouseEvent.getX() / (model.getTileSize() * getScale()));
            int tileY = (int) (mouseEvent.getY() / (model.getTileSize() * getScale()));
            tileHoverLabel.setText("x:" + tileX + " y:" + tileY);
        };
    }

    private EventHandler<MouseEvent> getTileImageInformationEventHandler() {
        LOGGER.info("getTileImageInformationEventHandler");
        return mouseEvent -> {
            int tileX = (int) (mouseEvent.getX() / (model.getTileSize() * getScale()));
            int tileY = (int) (mouseEvent.getY() / (model.getTileSize() * getScale()));
            if(model.getTilesPerRow() > tileX && model.getTilesPerColumn() > tileY) {
                tileImageInformations.setText(model.getDstTileInformation(tileX, tileY));
            }
        };
    }

    private void initChangeListener() {
        LOGGER.info("initChangeListener");

        scaleProperty().addListener((observable, oldValue, newValue) -> scaleTiles());

        preColorAlignment.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue, 0, 100);
            preColorAlignment.setText(String.valueOf(percent));
            model.setPreColorAlignment(percent);
        });
        model.preColorAlignmentProperty().addListener((observable, oldValue, newValue) -> preColorAlignment.setText(String.valueOf(newValue)));
        preColorAlignment.setText(String.valueOf(model.getPreColorAlignment()));

        postColorAlignment.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue, 0, 100);
            postColorAlignment.setText(String.valueOf(percent));
            model.setPostColorAlignment(percent);
        });
        model.postColorAlignmentProperty().addListener((observable, oldValue, newValue) -> postColorAlignment.setText(String.valueOf(newValue)));
        postColorAlignment.setText(String.valueOf(model.getPostColorAlignment()));

        opacity.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue, 0, 100);
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

        displayOriginalImageProperty().addListener((observable, oldValue, newValue) -> setTiles());

        model.tilesPerRowProperty().addListener((observable, oldValue, newValue) -> initTileViews());

        model.tilesPerColumnProperty().addListener((observable, oldValue, newValue) -> initTileViews());

        model.imageCalculatedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue) setTiles();
        });
    }


    private int getIntFromString(String value, int min, int max) {
        String digitString = value.replaceAll("[^\\d]", "");
        digitString = digitString.substring(0, Math.min(5, digitString.length()));
        return Math.min(Math.max(min, Integer.parseInt("0" + digitString)), max);
    }


    private EventHandler<ScrollEvent> getScrollEventHandler() {
        LOGGER.info("getScrollEventHandler");
        return scrollEvent -> {

            if (scrollEvent.isControlDown()) {
                setScale(getScale() * (1 + scrollEvent.getDeltaY() / 100));
                scrollEvent.consume();
            }
        };
    }

    private void initCanvas() {
        LOGGER.info("initCanvas");
        try {
            String filename = "test.png";
            model.setSrcImageFile(Path.of(getClass().getResource(filename).toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

//    private void drawImage() {
//        LOGGER.info("drawImage");
//        BufferedImage bufferedImage = (isDisplayOriginalImage()) ? model.getOriginalImage() : model.getCompositeImage();
//
//        canvasPane.setPrefWidth((int) (bufferedImage.getWidth() * getScale()));
//        canvasPane.setPrefHeight((int) (bufferedImage.getHeight() * getScale()));
//
//        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
//        GraphicsContext gc = canvas.getGraphicsContext2D();
//        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
//        gc.drawImage(image, 0, 0, canvas.getWidth(), canvas.getHeight());
//    }

    @FXML
    private void processExit() {
        LOGGER.info("processExit");
        Platform.exit();
    }

    @FXML
    private void showAboutDialog() {
        LOGGER.info("showAboutDialog");
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(menuBar.getScene().getWindow());
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().add(new Text("About"));
        Scene dialogScene = new Scene(dialogVbox, 300, 200);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    @FXML
    private void processOpen() {
        LOGGER.info("processOpen");
        FileChooser fileChooser = new FileChooser();
        if (getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().toFile());
        fileChooser.setTitle("Bild öffnen");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            LOGGER.info("path = " + path);
            if (Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.toString().endsWith("." + e))) {
                model.setSrcImageFile(path);
                setImagePath(path.getParent());
            }
        }
    }

    @FXML
    private void processTilesPath() {
        LOGGER.info("processTilesPath");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Wähle Mosaik Tile Pfad");
        directoryChooser.setInitialDirectory(model.getDstTilesPath().toFile());
        model.setDstTilesPath(directoryChooser.showDialog(menuBar.getScene().getWindow()).toPath());
    }

    @FXML
    private void dragDropped(DragEvent dragEvent) {
        LOGGER.info("dragDropped");
        Dragboard dragboard = dragEvent.getDragboard();
        boolean success = false;
        if (dragboard.hasFiles()) {
            if (hasDirectory(dragboard)) {
                File folder = dragboard.getFiles().get(0);
                model.setDstTilesPath(folder.toPath());
                success = true;
            } else if (hasImageFile(dragboard)) {
                Path path = dragboard.getFiles().get(0).toPath();
                if (isImage(path)) {
                    model.setSrcImageFile(path);
                    setImagePath(path.getParent());
                }
            }
        }

        dragEvent.setDropCompleted(success);

        dragEvent.consume();
    }

    @FXML
    private void dragOver(DragEvent dragEvent) {
        LOGGER.debug("dragOver");
        Dragboard dragboard = dragEvent.getDragboard();
        if (dragEvent.getGestureSource() != scrollPane) {

            if (dragboard.hasFiles()) {

                if (hasDirectory(dragboard) || hasImageFile(dragboard)) {
                    dragEvent.acceptTransferModes(TransferMode.ANY);
                }
            } else {
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

    @FXML
    private void recalculateImage() {
        LOGGER.info("recalculateImage");
        model.generateMosaicImage();
    }

    @FXML
    private void originalCheckAction() {
        LOGGER.info("originalCheckAction");
        setDisplayOriginalImage(originalCheck.isSelected());
    }

    @FXML
    private void processSave() {
        LOGGER.info("processSave");
        FileChooser fileChooser = new FileChooser();
        if (getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().toFile());
        fileChooser.setTitle("Bild speichern");
        if (getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().toFile());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
           if (Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.toString().endsWith("." + e))) {
                model.saveMosaicImage(path);
                setImagePath(path.getParent());
            }
        }
    }

    private DoubleProperty scaleProperty() {
        return scale;
    }

    private double getScale() {
        return scale.get();
    }

    private void setScale(double scale) {
        this.scale.set(Math.max(Math.min(scale, SCALE_MAX), SCALE_MIN));
    }

    private Path getImagePath() {
        return imagePath.get();
    }

    private void setImagePath(Path imagePath) {
        this.imagePath.set(imagePath);
    }

    private BooleanProperty displayOriginalImageProperty() {
        return displayOriginalImage;
    }

    private boolean isDisplayOriginalImage() {
        return displayOriginalImage.get();
    }

    private void setDisplayOriginalImage(boolean value) {
        displayOriginalImage.set(value);
    }

    @FXML public void gc() {
        System.gc();
    }
}
