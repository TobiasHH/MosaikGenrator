package de.tobiashh.javafx;

import de.tobiashh.javafx.model.MosaicImageModel;
import de.tobiashh.javafx.model.MosaicImageModelImpl;
import de.tobiashh.javafx.properties.PropertiesManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class Controller {
    private final static Logger LOGGER = LoggerFactory.getLogger(Controller.class.getName());
    private static final double SCALE_DEFAULT = 1.0;
    private static final double SCALE_MIN = 0.1;
    private static final double SCALE_MAX = 2.0;
    private static final int HIDDEN_TILE_SIZE = 16;
    public final MosaicImageModel model;
    private final DoubleProperty scale = new SimpleDoubleProperty(SCALE_DEFAULT);
    private final BooleanProperty displayOriginalImage = new SimpleBooleanProperty();
    private final BooleanProperty drawDebugInfo = new SimpleBooleanProperty();
    private final ObjectProperty<Path> imagePath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> saveImagePath = new SimpleObjectProperty<>(Path.of(System.getProperty("user.home")));
    private final StringProperty status = new SimpleStringProperty();

    PropertiesManager propertiesManager = new PropertiesManager();
    List<TileView> tiles = new ArrayList<>();
    @FXML
    public Label imageLabel;
    @FXML
    public Label compareSize;
    @FXML
    public ChoiceBox<Mode> modeChoiceBox;
    @FXML
    public Label imageTilesCount;
    @FXML
    public Button recalculateImageButton;
    @FXML
    public Button randomImageButton;
    @FXML
    private MenuBar menuBar;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    Label cursorPositionLabel;
    @FXML
    private Label pathLabel;
    @FXML
    private Label filesCountLabel;
    @FXML
    private Label tilesMinNeededLabel;
    @FXML
    private Label usedCountLabel;
    @FXML
    Label tileHoverLabel;
    @FXML
    Label tileImageInformations;
    @FXML
    private Pane canvasPane;
    @FXML
    private CheckBox originalCheck;
    @FXML
    private CheckBox scanSubfolderCheck;
    @FXML
    CheckBox areaOfInterestCheck;
    @FXML
    private CheckBox drawDebugInfoCheck;
    @FXML
    private TextField preColorAlignment;
    @FXML
    private TextField postColorAlignment;
    @FXML
    private TextField opacity;
    @FXML
    private TextField tilesPerRow;
    @FXML
    public CheckBox isTilesPerImageCheck;
    @FXML
    public TextField tilesPerImage;
    @FXML
    private TextField maxReuses;
    @FXML
    private TextField reuseDistance;
    @FXML
    private Label statusLabel;

    public Controller(MosaicImageModel model) {
        LOGGER.info("Controller");
        this.model = model;
        model.setController(this);
    }

    @FXML
    private void initialize() {
        LOGGER.info("initialize");
        initModeChoiceBox();
        initChangeListener();
        initEventHandler();
        initBindings();
        initCanvas();
        setDebugInfo(propertiesManager.drawDebugInfoProperty().get());
    }

    private void initModeChoiceBox() {
        modeChoiceBox.getItems().addAll(Mode.values());
    }

    private void initBindings() {
        LOGGER.info("initBindings");
        model.tilesPerColumnProperty().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> imageTilesCount.setText(String.valueOf(newValue.intValue() * model.tilesPerRowProperty().get()))));
        pathLabel.textProperty().bind(Bindings.when(propertiesManager.tilesPathProperty().isNull()).then("Kein Pfad gewählt.").otherwise(propertiesManager.tilesPathProperty().asString()));
        filesCountLabel.textProperty().bind(model.dstTilesCountProperty().asString());
        tilesMinNeededLabel.textProperty().bind(model.tilesMinNeededProperty().asString());
        compareSize.textProperty().bind(model.compareSizeProperty().asString());
        imageLabel.textProperty().bind(imagePath.asString());
        usedCountLabel.textProperty().bind(model.usedCountProperty().asString());
        modeChoiceBox.valueProperty().bindBidirectional(propertiesManager.modeProperty());
        scanSubfolderCheck.selectedProperty().bindBidirectional(propertiesManager.scanSubFolderProperty());
        drawDebugInfoCheck.selectedProperty().bindBidirectional(propertiesManager.drawDebugInfoProperty());
        isTilesPerImageCheck.selectedProperty().bindBidirectional(propertiesManager.isTilesPerImageProperty());
        statusLabel.textProperty().bind(status);
        tilesPerRow.disableProperty().bind(isTilesPerImageCheck.selectedProperty());
        tilesPerImage.disableProperty().bind(isTilesPerImageCheck.selectedProperty().not());

        initTextFieldBindings();

        model.tilesPerRowProperty().bind(propertiesManager.tilesPerRowProperty());
        model.tilesPerImageProperty().bind(propertiesManager.tilesPerImageProperty());
        model.tilesPerRowProperty().bind(propertiesManager.tilesPerRowProperty());
        model.tileSizeProperty().bind(propertiesManager.tileSizeProperty());
        model.opacityProperty().bind(propertiesManager.opacityProperty());
        model.preColorAlignmentProperty().bind(propertiesManager.preColorAlignmentProperty());
        model.postColorAlignmentProperty().bind(propertiesManager.postColorAlignmentProperty());
        model.modeProperty().bind(propertiesManager.modeProperty());
        model.reuseDistanceProperty().bind(propertiesManager.reuseDistanceProperty());
        model.maxReusesProperty().bind(propertiesManager.maxReusesProperty());
        model.compareSizeProperty().bind(propertiesManager.compareSizeProperty());
        model.scanSubFolderProperty().bind(propertiesManager.scanSubFolderProperty());
        model.drawDebugInfoProperty().bind(propertiesManager.drawDebugInfoProperty());
        model.isTilesPerImageProperty().bind(propertiesManager.isTilesPerImageProperty());

        model.tilesPathProperty().bind(propertiesManager.tilesPathProperty());
        model.cachePathProperty().bind(propertiesManager.cachePathProperty());
        model.srcImagePathProperty().bind(imagePath);

//        scrollPane.viewportBoundsProperty().addListener((observable, oldValue, newValue) -> manageVisibility());
//        scrollPane.getContent().boundsInParentProperty().addListener((observable, oldValue, newValue) -> manageVisibility());
//        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> manageVisibility());
//        scrollPane.hvalueProperty().addListener((observable, oldValue, newValue) -> manageVisibility());
    }

    private void manageVisibility() {
        double hmin = scrollPane.getHmin();
        double hmax = scrollPane.getHmax();
        double hvalue = scrollPane.getHvalue();
        double contentWidth = scrollPane.getContent().getLayoutBounds().getWidth();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();

        double hoffset = Math.max(0, contentWidth - viewportWidth) * (hvalue - hmin) / (hmax - hmin);

        double vmin = scrollPane.getVmin();
        double vmax = scrollPane.getVmax();
        double vvalue = scrollPane.getVvalue();
        double contentHeight = scrollPane.getContent().getLayoutBounds().getHeight();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        double voffset = Math.max(0, contentHeight - viewportHeight) * (vvalue - vmin) / (vmax - vmin);

        int x = (int) (hoffset / getActualTileSize());
        int y = (int) (voffset / getActualTileSize());
        int tilesOnScreenW = (int) (viewportWidth / getActualTileSize()) + 1;
        int tilesOnScreenH = (int) (viewportHeight / getActualTileSize()) + 1;

        // TODO mechanismus, der die größe entsprechend lazyLoad anpasst, zum beispiel durch future

        tiles.forEach(tile -> {
            if (tile.getImage() != null) {
                if (isVisibleTile(x, y, tilesOnScreenW, tilesOnScreenH, tile)) {

                    int nextPowerOfTwoImage = getNextPowerOfTwo(tile.getImage().getWidth());
                    int nextPowerOfTwoImageView = getNextPowerOfTwo(tile.getFitWidth());

                    if (nextPowerOfTwoImage != nextPowerOfTwoImageView) {
                        LOGGER.debug("get new visible image for " + tile.getTilePositionX() + ", " + tile.getTilePositionY());
                        //         tile.setTile(model.getTile(tile.getTilePositionX(), tile.getTilePositionY(), isDisplayOriginalImage(), nextPowerOfTwoImageView));
                    }
                } else {
                    if (tile.getImage().getWidth() != HIDDEN_TILE_SIZE) {
                        LOGGER.debug("get new hidden image for " + tile.getTilePositionX() + ", " + tile.getTilePositionY());
                        //        tile.setTile(model.getTile(tile.getTilePositionX(), tile.getTilePositionY(), isDisplayOriginalImage(), HIDDEN_TILE_SIZE));
                    }
                }
            }
        });

        LOGGER.debug("visibleTiles = " + tiles.stream().filter(tile -> isVisibleTile(x, y, tilesOnScreenW, tilesOnScreenH, tile)).count());
        LOGGER.debug("tilesWithHiddenWidth = " + tiles.stream().filter(tile -> tile.getImage() != null && tile.getImage().getWidth() == HIDDEN_TILE_SIZE).count());
        tiles.stream().map(tile -> tile.getImage() != null ? tile.getImage().getWidth() : 0).sorted().distinct().forEach(size -> LOGGER.debug(size + ": " + tiles.stream().filter(tile -> size == (tile.getImage() != null ? tile.getImage().getWidth() : 0)).count()));

    }

    private boolean isVisibleTile(int x, int y, int tilesOnScreenW, int tilesOnScreenH, TileView tile) {
        return tile.getImage() != null && tile.getTilePositionX() >= x && tile.getTilePositionX() <= x + tilesOnScreenW && tile.getTilePositionY() >= y && tile.getTilePositionY() <= y + tilesOnScreenH;
    }

    private int getNextPowerOfTwo(double tileSize) {
        int pow = 1;
        while ((int) Math.pow(2, pow) < tileSize) {
            pow++;
        }
        return (int) Math.pow(2, pow);
    }

    private void scaleTiles() {
        int tileSize = getActualTileSize();
        tiles.forEach(tileView -> tileView.setTileSize(tileSize));
        canvasPane.setPrefWidth(tileSize * propertiesManager.tilesPerRowProperty().get());
        canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
    }

    private int getActualTileSize() {
        return (int) (propertiesManager.tileSizeProperty().get() * getScale());
    }

    void setTiles(List<TileView> tileViews) {
        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            tileViews.forEach(tileView -> {
                int x = tileView.getTilePositionX();
                int y = tileView.getTilePositionY();
                GetTileTask getTileTask = new GetTileTask(model, x, y, isDisplayOriginalImage());
                getTileTask.setOnSucceeded(event -> Platform.runLater(() -> tileView.setTile(getTileTask.getValue())));
                executorService.execute(getTileTask);
            });
            executorService.shutdown();
        }
    }

    private void initTileViews() {
        int tilesPerRow = propertiesManager.tilesPerRowProperty().get();
        int tilesPerColumn = model.getTilesPerColumn();

        tiles.clear();

        int tileSize = getActualTileSize();

        for (int y = 0; y < tilesPerColumn; y++) {
            for (int x = 0; x < tilesPerRow; x++) {

                TileView tileView = new TileView(x, y, tileSize);
                tileView.setFitWidth(tileSize);
                tileView.setFitHeight(tileSize);

                tiles.add(tileView);
            }
        }

        if (Platform.isFxApplicationThread()) {
            initCanvas(tileSize);
        } else {
            Platform.runLater(() -> initCanvas(tileSize));
        }
    }

    private void initCanvas(int tileSize) {
        System.out.println("initCanvas");
        System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
        canvasPane.getChildren().clear();
        canvasPane.getChildren().addAll(tiles);
        canvasPane.setPrefWidth(tileSize * propertiesManager.tilesPerRowProperty().get());
        canvasPane.setPrefHeight(tileSize * model.getTilesPerColumn());
    }

    private void initEventHandler() {
        LOGGER.info("initEventHandler");
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, new CursorPositionEventHandler(this));
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, new TileHoverEventHandler(this));
        canvasPane.addEventHandler(MouseEvent.MOUSE_MOVED, new TileImageInformationEventHandler(this));
        canvasPane.addEventHandler(MouseEvent.MOUSE_CLICKED, new TileClickedEventHandler(this));
        scrollPane.addEventFilter(ScrollEvent.SCROLL, new ScrollEventHandler(this));
        DragEventHandler dragEventHandler = new DragEventHandler(this);
        scrollPane.addEventFilter(MouseEvent.DRAG_DETECTED, dragEventHandler);
        scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, dragEventHandler);
        scrollPane.addEventFilter(MouseDragEvent.MOUSE_DRAG_RELEASED, dragEventHandler);
    }

    int getTilePosition(double mousePosition) {
        return (int) (mousePosition / getActualTileSize());
    }

    private void initChangeListener() {
        LOGGER.info("initChangeListener");
        initControllerPropertyChangeListener();
        initPropertiesManagerChangeListener();
        initModelChangeListener();
    }

    private void initModelChangeListener() {
        model.tilesPerColumnProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Controller.initModelChangeListener.tilesPerColumnProperty");
            model.resetAreaOfIntrest();
            initTileViews();
        });
        model.imageCalculatedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) setTiles(tiles);
        });
    }

    private void initPropertiesManagerChangeListener() {
        propertiesManager.tilesPerRowProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Controller.initPropertiesManagerChangeListener");
            if(!propertiesManager.isTilesPerImageProperty().get()) {
                model.resetAreaOfIntrest();
                initTileViews();
            }
        });
    }

    private void initControllerPropertyChangeListener() {
        scaleProperty().addListener((observable, oldValue, newValue) -> scaleTiles());
        displayOriginalImageProperty().addListener((observable, oldValue, newValue) -> setTiles(tiles));
        drawDebugInfoProperty().addListener((observable, oldValue, newValue) -> setTiles(tiles));
    }

    private void initTextFieldBindings() {
        initIntegerTextFieldBinding(preColorAlignment, propertiesManager.preColorAlignmentProperty(), 0, 100);
        initIntegerTextFieldBinding(postColorAlignment, propertiesManager.postColorAlignmentProperty(), 0, 100);
        initIntegerTextFieldBinding(opacity, propertiesManager.opacityProperty(), 0, 100);
        initIntegerTextFieldBinding(tilesPerRow, propertiesManager.tilesPerRowProperty(), 1, 50);
        initIntegerTextFieldBinding(tilesPerImage, propertiesManager.tilesPerImageProperty(), 1, 2000);
        initIntegerTextFieldBinding(maxReuses, propertiesManager.maxReusesProperty(), 0, 5000);
        initIntegerTextFieldBinding(reuseDistance, propertiesManager.reuseDistanceProperty(), 1, 50);
    }

    private void initIntegerTextFieldBinding(TextField textField, ObjectProperty<Integer> property, int minValue, int maxValue) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            int percent = getIntFromString(newValue, minValue, maxValue);
            textField.setText(String.valueOf(percent));
            property.set(percent);
        });
        property.addListener((observable, oldValue, newValue) -> textField.setText(String.valueOf(newValue)));
        textField.setText(String.valueOf(property.get()));
    }

    private int getIntFromString(String value, int min, int max) {
        String digitString = value.replaceAll("\\D", "");
        digitString = digitString.substring(0, Math.min(5, digitString.length()));
        return Math.min(Math.max(min, Integer.parseInt("0" + digitString)), max);
    }

    private void initCanvas() {
        LOGGER.info("initCanvas");
        try {
            String filename = "test.png";
            imagePath.set(Path.of(getClass().getResource(filename).toURI()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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

        if (getImagePath() != null && !getImagePath().endsWith("test.png")) {
            LOGGER.info("imagePath");
            File file = getImagePath().getParent().toFile();
            if(file.isDirectory()) fileChooser.setInitialDirectory(file);
        } else {
            LOGGER.info("tilesPath");
            File file = propertiesManager.tilesPathProperty().get().toFile();
            if(file.isDirectory()) fileChooser.setInitialDirectory(file);
        }

        fileChooser.setTitle("Bild öffnen");
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
        directoryChooser.setTitle("Wähle Mosaik Tile Pfad");
        File file = propertiesManager.tilesPathProperty().get().toFile();
        if(file.isDirectory()) directoryChooser.setInitialDirectory(file);
        file = directoryChooser.showDialog(menuBar.getScene().getWindow());
        if (file != null) propertiesManager.tilesPathProperty().set(file.toPath());
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
    private void debugInfoCheckAction() {
        LOGGER.info("debugInfoCheckAction");
        setDebugInfo(drawDebugInfoCheck.isSelected());
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

    double getScale() {
        return scale.get();
    }

    void setScale(double scale) {
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

    public void setStatus(String status) {
        Platform.runLater(() -> this.status.set(status));
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

    private BooleanProperty drawDebugInfoProperty() {
        return drawDebugInfo;
    }

    private void setDebugInfo(boolean value) {
        drawDebugInfo.set(value);
    }

    @FXML
    public void gc() {
        System.gc();
    }

    @FXML
    public void randomImage() {
        Path tilePath = model.tilesPathProperty().get();
        boolean scanSubFolder = model.scanSubFolderProperty().get();
        try (Stream<Path> pathStream = scanSubFolder ? Files.walk(tilePath) : Files.list(tilePath)) {
            List<Path> paths = pathStream.filter(this::extensionFilter).toList();
            Path randomPath = paths.get(new Random().nextInt(paths.size()));
            setImagePath(randomPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private boolean extensionFilter(Path path) {
        return Arrays
                .stream(MosaicImageModelImpl.FILE_EXTENSION)
                .anyMatch(e -> path.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())));
    }

    public void setTilesPerRow(int tilesPerRow) {
        propertiesManager.tilesPerRowProperty().set(tilesPerRow);
    }

    public void setTilesPerImage(int tilesPerImage) {
        propertiesManager.tilesPerImageProperty().set(tilesPerImage);
    }
}
