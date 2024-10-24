package de.tobiashh.javafx;

import de.tobiashh.javafx.tools.Converter;
import de.tobiashh.javafx.tools.Position;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileClickedEventHandler implements javafx.event.EventHandler<MouseEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TileClickedEventHandler.class.getName());
    private final Controller controller;

    public TileClickedEventHandler(Controller controller) {
        LOGGER.info("Erstelle TileClickedEventHandler");
        this.controller = controller;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        LOGGER.info("StartTileEventHandler");
        if (mouseEvent.isStillSincePress()) {
            int x = controller.getTilePosition(mouseEvent.getX());
            int y = controller.getTilePosition(mouseEvent.getY());
            MouseButton button = mouseEvent.getButton();

            if (controller.areaOfInterestCheck.isSelected()) {
                if (button.equals(MouseButton.PRIMARY)) controller.model.addAreaOfIntrest(x, y);
                if (button.equals(MouseButton.SECONDARY))  controller.model.removeAreaOfIntrest(x, y);
                int index = new Converter(controller.model.tilesPerRowProperty().get()).getIndex(new Position(x, y));
                controller.setTiles(controller.tiles.subList(index, index + 1));
            }

            if (!controller.areaOfInterestCheck.isSelected()) {
                if (button.equals(MouseButton.PRIMARY)) controller.model.replaceTile(x, y);
                if (button.equals(MouseButton.SECONDARY)) controller.model.ignoreTile(x, y);
                controller.setTiles(controller.tiles);
            }

            mouseEvent.consume();
        }
    }
}
