package de.tobiashh.javafx;

import de.tobiashh.javafx.model.MosaicImageModel;
import de.tobiashh.javafx.model.MosaicImageModelImpl;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.BoundingBox;
import javafx.scene.Scene;
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

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {
    private final static Logger LOGGER = LoggerFactory.getLogger(Controller.class.getName());
    private static final double SCALE_DEFAULT = 1.0;
    private static final double SCALE_MIN = 0.1;
    private static final double SCALE_MAX = 10.0;
    private final MosaicImageModel model;
    private final DoubleProperty scale = new SimpleDoubleProperty(SCALE_DEFAULT);
    private final BooleanProperty displayOriginalImage = new SimpleBooleanProperty();
    private final ObjectProperty<Path> imagePath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> saveImagePath = new SimpleObjectProperty<>(Path.of(System.getProperty("user.home")));

    @FXML
    public ChoiceBox<Mode> modeChoiceBox;
    @FXML
    public Label imageTilesCount;
    PropertiesManager propertiesManager = new PropertiesManager();
    List<TileView> tiles = new ArrayList<>();
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

    private boolean setAreaOfInterest;

    public Controller(MosaicImageModel model) {
        LOGGER.info("Controller");
        this.model = model;
    }

    @FXML
    private void initialize() {
        LOGGER.info("initialize");
        initModeChoiceBox();
        initChangeListener();
        initEventHandler();
        initBindings();
        initCanvas();
    }

    private void initModeChoiceBox() {
        modeChoiceBox.getItems().addAll(Mode.values());
    }

    private void initBindings() {
        LOGGER.info("initBindings");
        model.tilesPerColumnProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> imageTilesCount.setText(String.valueOf(newValue.intValue() * model.tilesPerRowProperty().get()))));

        pathLabel.textProperty().bind(Bindings.when(propertiesManager.tilesPathProperty().isNull()).then("Kein Pfad gew??hlt.").otherwise(propertiesManager.tilesPathProperty().asString()));
        filesCountLabel.textProperty().bind(model.dstTilesCountProperty().asString());
        modeChoiceBox.valueProperty().bindBidirectional(propertiesManager.modeProperty());
        scanSubfolderCheck.selectedProperty().bindBidirectional(propertiesManager.scanSubFolderProperty());
        statusLabel.textProperty().bind(model.statusProperty());

        initTextFieldBindings();

        model.tilesPerRowProperty().bind(propertiesManager.tilesPerRowProperty());
        model.tileSizeProperty().bind(propertiesManager.tileSizeProperty());
        model.opacityProperty().bind(propertiesManager.opacityProperty());
        model.postColorAlignmentProperty().bind(propertiesManager.postColorAlignmentProperty());
        model.modeProperty().bind(propertiesManager.modeProperty());
        model.reuseDistanceProperty().bind(propertiesManager.reuseDistanceProperty());
        model.maxReusesProperty().bind(propertiesManager.maxReusesProperty());
        model.compareSizeProperty().bind(propertiesManager.compareSizeProperty());
        model.scanSubFolderProperty().bind(propertiesManager.scanSubFolderProperty());

        model.tilesPathProperty().bind(propertiesManager.tilesPathProperty());
        model.srcImagePathProperty().bind(imagePath);

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> countVisibleTiles());
        scrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> countVisibleTiles());
        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> countVisibleTiles());
        scrollPane.heightProperty().addListener((observable, oldValue, newValue) -> countVisibleTiles());
    }

    // todo Wird noch interessant beim laden von bilder nach opacity und co () nur visible updaten
    private void countVisibleTiles() {
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

        long count = tiles.stream().filter(tileView -> tileView.intersects(new BoundingBox(hoffset, voffset, viewportWidth, viewportHeight))).count();
        String tileCoordinates = tiles.stream().filter(tileView -> tileView.intersects(new BoundingBox(hoffset, voffset, viewportWidth, viewportHeight)))
                .map(tileView -> "" + tileView.getTilePositionX() + "," + tileView.getTilePositionY()).collect(Collectors.joining(" "));

        LOGGER.info("visible tiles count = " + count);
        LOGGER.info("tileCoordinates = " + tileCoordinates);
    }

    private void scaleTiles() {
        int tileSize = (int) (propertiesManager.tileSizeProperty().get() * getScale());
        tiles.forEach(tileView -> tileView.setTileSize(tileSize));
        canvasPane.setPrefWidth(tileSize * propertiesManager.tilesPerRowProperty().get());
        canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
    }

    private void setTiles() {
        tiles.forEach(tileView -> {
            int x = tileView.getTilePositionX();
            int y = tileView.getTilePositionY();

            tileView.setTile((isDisplayOriginalImage()) ? model.getOriginalTile(x, y) : model.getTile(x, y));
        });
    }

    private void initTileViews() {
        int tilesPerRow = propertiesManager.tilesPerRowProperty().get();
        int tilesPerColumn = model.getTilesPerColumn();

        tiles.clear();

        int tileSize = (int) (getScale() * propertiesManager.tileSizeProperty().get());

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {

                TileView tileView = new TileView(x, y, tileSize);
                tileView.setFitWidth(tileSize);
                tileView.setFitHeight(tileSize);

                tiles.add(tileView);
            }
        }

        if (Platform.isFxApplicationThread()) {
            canvasPane.getChildren().clear();
            canvasPane.getChildren().addAll(tiles);
            canvasPane.setPrefWidth(tileSize * propertiesManager.tilesPerRowProperty().get());
            canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
        } else {
            Platform.runLater(() -> {
                canvasPane.getChildren().clear();
                canvasPane.getChildren().addAll(tiles);
                canvasPane.setPrefWidth(tileSize * propertiesManager.tilesPerRowProperty().get());
                canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
            });
        }
    }

    private void initEventHandler() {
        LOGGER.info("initEventHandler");
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, getCursorPositionEventHandler());
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, getTileHoverEventHandler());
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, getTileImageInformationEventHandler());
        canvasPane.addEventHandler(MouseEvent.MOUSE_PRESSED, setStartTileWithSecondaryButtonDown());
        canvasPane.addEventHandler(MouseEvent.MOUSE_DRAGGED, markTilesWithSecondaryButtonDown());
        scrollPane.addEventFilter(ScrollEvent.SCROLL, getScrollEventHandler());
    }

    private EventHandler<MouseEvent> setStartTileWithSecondaryButtonDown() {
        return mouseEvent -> {
            if(mouseEvent.isSecondaryButtonDown())
            {
                int x = (int) (mouseEvent.getX() / (propertiesManager.tileSizeProperty().get() * getScale()));
                int y = (int) (mouseEvent.getY() / (propertiesManager.tileSizeProperty().get() * getScale()));
                setAreaOfInterest = !model.isAreaOfInterest(x, y);

                mouseEvent.consume();
            }
        };
    }

    private EventHandler<MouseEvent> markTilesWithSecondaryButtonDown() {
        return mouseEvent -> {
            if(mouseEvent.isSecondaryButtonDown())
            {
                int x = (int) (mouseEvent.getX() / (propertiesManager.tileSizeProperty().get() * getScale()));
                int y = (int) (mouseEvent.getY() / (propertiesManager.tileSizeProperty().get() * getScale()));

                if(setAreaOfInterest)
                {
                    model.addAreaOfIntrest(x, y);
                }
                else
                {
                    model.removeAreaOfIntrest(x, y);
                }

                mouseEvent.consume();
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
            int tileX = (int) (mouseEvent.getX() / (propertiesManager.tileSizeProperty().get() * getScale()));
            int tileY = (int) (mouseEvent.getY() / (propertiesManager.tileSizeProperty().get() * getScale()));
            tileHoverLabel.setText("x:" + tileX + " y:" + tileY);
        };
    }

    private EventHandler<MouseEvent> getTileImageInformationEventHandler() {
        LOGGER.info("getTileImageInformationEventHandler");
        return mouseEvent -> {
            int tileX = (int) (mouseEvent.getX() / (propertiesManager.tileSizeProperty().get() * getScale()));
            int tileY = (int) (mouseEvent.getY() / (propertiesManager.tileSizeProperty().get() * getScale()));
            if (propertiesManager.tilesPerRowProperty().get() > tileX && model.getTilesPerColumn() > tileY) {
                tileImageInformations.setText(model.getDstTileInformation(tileX, tileY));
            }
        };
    }

    private void initChangeListener() {
        LOGGER.info("initChangeListener");
        initControllerPropertyChangeListener();
        initPropertiesManagerChangeListener();
        initModelChangeListener();
    }

    private void initModelChangeListener() {
        model.tilesPerColumnProperty().addListener((observable, oldValue, newValue) -> initTileViews());
        model.imageCalculatedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) setTiles();
        });
    }

    private void initPropertiesManagerChangeListener() {
        propertiesManager.tilesPerRowProperty().addListener((observable, oldValue, newValue) -> {
            resetAreaOfInterest();
            initTileViews();
        });
    }

    private void resetAreaOfInterest() {
        model.resetAreaOfIntrest();
    }

    private void initControllerPropertyChangeListener() {
        scaleProperty().addListener((observable, oldValue, newValue) -> scaleTiles());
        displayOriginalImageProperty().addListener((observable, oldValue, newValue) -> setTiles());
    }

    private void initTextFieldBindings() {
        initIntegerTextFieldBinding(preColorAlignment, propertiesManager.preColorAlignmentProperty(), 0, 100);
        initIntegerTextFieldBinding(postColorAlignment, propertiesManager.postColorAlignmentProperty(), 0, 100);
        initIntegerTextFieldBinding(opacity, propertiesManager.opacityProperty(), 0, 100);
        initIntegerTextFieldBinding(tilesPerRow, propertiesManager.tilesPerRowProperty(), 1, 50);
        initIntegerTextFieldBinding(maxReuses, propertiesManager.maxReusesProperty(), 0, 5000);
        initIntegerTextFieldBinding(reuseDistance, propertiesManager.reuseDistanceProperty(), 1, 50);
    }

    private void initIntegerTextFieldBinding(TextField textField, IntegerProperty property, int minValue, int maxValue) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue, minValue, maxValue);
            textField.setText(String.valueOf(percent));
            property.set(percent);
        });
        property.addListener((observable, oldValue, newValue) -> textField.setText(String.valueOf(newValue)));
        textField.setText(String.valueOf(property.get()));
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
            imagePath.set(Path.of(getClass().getResource(filename).toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

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
        if (getImagePath() != null) fileChooser.setInitialDirectory(getImagePath().getParent().toFile());
        fileChooser.setTitle("Bild ??ffnen");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        File file = fileChooser.showOpenDialog(menuBar.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            LOGGER.info("path = " + path);
            if (Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.toString().endsWith("." + e))) {
                setImagePath(path);
            }
        }
    }

    @FXML
    private void processTilesPath() {
        LOGGER.info("processTilesPath");
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("W??hle Mosaik Tile Pfad");
        directoryChooser.setInitialDirectory(propertiesManager.tilesPathProperty().get().toFile());
        propertiesManager.tilesPathProperty().set(directoryChooser.showDialog(menuBar.getScene().getWindow()).toPath());
    }

    @FXML
    private void dragDropped(DragEvent dragEvent) {
        LOGGER.info("dragDropped");
        Dragboard dragboard = dragEvent.getDragboard();
        if (dragboard.getFiles().size() == 1) {
            File file = dragboard.getFiles().get(0);
            if (isDirectory(file)) {
                propertiesManager.tilesPathProperty().set(file.toPath());
                dragEvent.setDropCompleted(true);
            } else if (isImage(file)) {
                setImagePath(file.toPath());
                dragEvent.setDropCompleted(true);
            }
        }

        dragEvent.consume();
    }

    @FXML
    private void dragOver(DragEvent dragEvent) {
        LOGGER.debug("dragOver");
        Dragboard dragboard = dragEvent.getDragboard();

        if (dragboard.getFiles().size() == 1) {
            File file = dragboard.getFiles().get(0);
            if (isDirectory(file) || isImage(file)) {
                dragEvent.acceptTransferModes(TransferMode.ANY);
            }
        }

        dragEvent.consume();
    }

    private boolean isDirectory(File file) {
        return file.isDirectory();
    }

    private boolean isImage(File file) {
        return Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> file.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())));
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
        fileChooser.setTitle("Bild speichern");

        if (getImagePath() != null) fileChooser.setInitialDirectory(getSaveImagePath().getParent().toFile());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Bilder", Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).map("*."::concat).toArray(String[]::new)));
        File file = fileChooser.showSaveDialog(menuBar.getScene().getWindow());
        if (file != null) {
            Path path = file.toPath();
            if (Arrays.stream(MosaicImageModelImpl.FILE_EXTENSION).anyMatch(e -> path.toString().endsWith("." + e))) {
                model.saveMosaicImage(path);
                setSaveImagePath(path.getParent());
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

    private Path getSaveImagePath() {
        return saveImagePath.get();
    }

    private void setSaveImagePath(Path imagePath) {
        this.saveImagePath.set(imagePath);
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

    @FXML
    public void gc() {
        System.gc();
    }
}
