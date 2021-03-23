package de.tobiashh.javafx;

import javafx.beans.property.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class MosaikImageModelImpl implements MosaikImageModel {
    private static final Path PROPERTY_FILE = Path.of("mosaik.properties");

    public static final String[] FILE_EXTENSION = {"png", "jpg", "jpeg"};

    private static final String TILES_X_PROPERTY_KEY = "tilesX";
    private static final int TILES_X_DEFAULT = 20;

    private static final String TILE_SIZE_PROPERTY_KEY = "tileSize";
    private static final int TILE_SIZE_DEFAULT = 128;

    private static final String TILES_PATH_PROPERRY_KEY = "tilesPath";
    private static final String TILES_PATH_DEFAULT = "tiles";

    private static final int TILE_SIZE_MIN = 2;
    private static final int TILE_SIZE_MAX = 512;


    private final ObjectProperty<Path> tilesPath = new SimpleObjectProperty<>();

    private final IntegerProperty tileSize = new SimpleIntegerProperty();

    private final ObjectProperty<Path> imageFile = new SimpleObjectProperty<>();

    private final IntegerProperty tilesX = new SimpleIntegerProperty();

    private final ReadOnlyIntegerWrapper tilesY = new ReadOnlyIntegerWrapper();

    private final ReadOnlyIntegerWrapper tileCount = new ReadOnlyIntegerWrapper();

    private final ReadOnlyLongWrapper filesCount = new ReadOnlyLongWrapper();

    private final ReadOnlyObjectWrapper<BufferedImage> compositeImage = new ReadOnlyObjectWrapper<>();

    private final List<Tile> tiles = new ArrayList<>();

    private final ImageComparator imageComparator = new ImageComparator();

    private Properties properties = new Properties();

    public MosaikImageModelImpl() {
        initChangeListener();
        initProperties();
    }

    private void initProperties() {
        System.out.println("MosaikImageModelImpl.initProperties");

        tileSize.addListener((observable, oldValue, newValue) -> changeProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(newValue.intValue())));
        tilesX.addListener((observable, oldValue, newValue) -> changeProperty(TILES_X_PROPERTY_KEY, String.valueOf(newValue.intValue())));
        tilesPath.addListener((observable, oldValue, newValue) -> changeProperty(TILES_PATH_PROPERRY_KEY, newValue.toString()));

        loadProperties();

        setTileSize(Integer.parseInt(properties.getProperty(TILE_SIZE_PROPERTY_KEY, String.valueOf(TILE_SIZE_DEFAULT))));
        setTilesX(Integer.parseInt(properties.getProperty(TILES_X_PROPERTY_KEY, String.valueOf(TILES_X_DEFAULT))));
        setTilesPath(Path.of(properties.getProperty(TILES_PATH_PROPERRY_KEY, TILES_PATH_DEFAULT)));
    }

    private void changeProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }

    private void saveProperties() {
        System.out.println("MosaikImageModelImpl.saveProperties");
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(PROPERTY_FILE.toFile()))) {
            properties.store(stream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadProperties() {
        System.out.println("MosaikImageModelImpl.loadProperties");
        if (Files.exists(PROPERTY_FILE)) {
            try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(PROPERTY_FILE.toFile()))) {
                properties.load(stream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initChangeListener() {
        tilesPath.addListener((observableValue, oldPath, newPath) -> calculateImagesCount(newPath));
        imageFile.addListener((observable, oldImageFile, newImageFile) -> loadImage(newImageFile));
    }


    private void generateTiles(BufferedImage image) {
        System.out.println("MosaikImageModelImpl.generateTiles");
        System.out.println("image = " + image);
        tiles.clear();
        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < getTilesX(); x++) {
                int tileSize = getTileSize();
                BufferedImage subImage = image.getSubimage(x * tileSize, y * tileSize, tileSize, tileSize);
                tiles.add(new Tile(subImage));
            }
        }
    }

    private void loadImage(Path imageFile) {
        System.out.println("MosaikImageModelImpl.loadImage");
        System.out.println("imageFile = " + imageFile);
        try {
            BufferedImage bufferedImage = ImageIO.read(imageFile.toFile());

            tilesY.set(getTilesX() * bufferedImage.getHeight() / bufferedImage.getWidth());

            int imageWidth = getTilesX() * getTileSize();
            int imageHeight = getTilesY() * getTileSize();

            generateTiles(ImageTools.calculateScaledImage(bufferedImage, imageWidth, imageHeight, true));
            compositeImage.set(calculateCompositeImage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTile(int x, int y) {
        System.out.println("MosaikImageModelImpl.deleteTile");
        System.out.println("x = " + x + ", y = " + y);
        tiles.set(x + y * getTilesX(), null);
        compositeImage.set(calculateCompositeImage());
    }

    private BufferedImage calculateCompositeImage() {
        System.out.println("MosaikImageModelImpl.calculateCompositeImage");
        BufferedImage retval = new BufferedImage(getTileSize() * getTilesX(), getTileSize() * getTilesY(), BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = (Graphics2D) retval.getGraphics();

        graphics.setColor(Color.RED);
        graphics.fillRect(0, 0, retval.getWidth(), retval.getHeight());

        for (int y = 0; y < getTilesY(); y++) {
            for (int x = 0; x < getTilesX(); x++) {
                int index = x + y * getTilesX();
                if (tiles.get(index) != null) {
                    graphics.drawImage(tiles.get(index).image, x * getTileSize(), y * getTileSize(), null);
                }
            }
        }

/*        Random r = new Random();
        for(int i = 0; i < 1000; i++) {
            int x = r.nextInt(getTilesX());
            int y = r.nextInt(getTilesY());
            int tile = r.nextInt(getTilesX() * getTilesY());
            if(tiles.get(tile) != null) {
                graphics.drawImage(tiles.get(tile).image, x * getTileSize(), y * getTileSize(), null);
            }
        }*/

        return retval;
    }

    private void setPowerOf2TileSize(int tileSize) {
        if (tileSize <= 0) throw new IllegalArgumentException();
        int nearestPowerOf2 = Integer.highestOneBit(tileSize) + (tileSize & Integer.highestOneBit(tileSize) >> 1) * 2;
        this.tileSize.set(Math.max(Math.min(nearestPowerOf2, TILE_SIZE_MAX), TILE_SIZE_MIN));
    }

    private void calculateImagesCount(Path newPath) {
        System.out.println("MosaikImageModelImpl.calculateImagesCount");
        System.out.println("newPath = " + newPath);

        if (Files.exists(newPath)) {
            try (Stream<Path> stream = Files.walk(newPath)) {
                filesCount.set(stream.filter(path -> Arrays.stream(FILE_EXTENSION).anyMatch(e -> path.toString().toLowerCase().endsWith(".".concat(e.toLowerCase())))).count());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public ReadOnlyObjectProperty<BufferedImage> compositeImageProperty() {
        return compositeImage.getReadOnlyProperty();
    }

    @Override
    public BufferedImage getCompositeImage() {
        return compositeImage.get();
    }

    @Override
    public ReadOnlyLongProperty filesCountProperty() {
        return filesCount.getReadOnlyProperty();
    }

    @Override
    public long getFilesCount() {
        return filesCount.get();
    }

    @Override
    public ReadOnlyIntegerProperty tileCountProperty() {
        return tileCount.getReadOnlyProperty();
    }

    @Override
    public int getTileCount() {
        return tileCount.get();
    }

    @Override
    public ObjectProperty<Path> imageFileProperty() {
        return imageFile;
    }

    @Override
    public Path getImageFile() {
        return imageFile.get();
    }

    @Override
    public void setImageFile(Path file) {
        imageFile.set(file);
    }

    @Override
    public IntegerProperty tileSizeProperty() {
        return tileSize;
    }

    @Override
    public int getTileSize() {
        return tileSize.get();
    }

    @Override
    public void setTileSize(int tileSize) {
        setPowerOf2TileSize(tileSize);
    }

    @Override
    public ObjectProperty<Path> tilesPathProperty() {
        return tilesPath;
    }

    @Override
    public Path getTilesPath() {
        return tilesPath.get();
    }

    @Override
    public void setTilesPath(Path tilesPath) {
        this.tilesPath.set(tilesPath);
    }

    @Override
    public IntegerProperty tilesXProperty() {
        return tilesX;
    }

    @Override
    public int getTilesX() {
        return tilesX.get();
    }

    @Override
    public void setTilesX(int length) {
        tilesX.set(length);
    }

    public ReadOnlyIntegerProperty tilesYProperty() {
        return tilesY.getReadOnlyProperty();
    }

    public int getTilesY() {
        return tilesY.get();
    }
}
