package de.tobiashh.javafx;

import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileImageInformationEventHandler implements javafx.event.EventHandler<MouseEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TileImageInformationEventHandler.class.getName());
    private final Controller controller;

    public TileImageInformationEventHandler(Controller controller) {
        LOGGER.info("Erstelle TileImageInformationEventHandler");
        this.controller = controller;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        int tileX = controller.getTilePosition(mouseEvent.getX());
        int tileY = controller.getTilePosition(mouseEvent.getY());
        if (controller.propertiesManager.tilesPerRowProperty().get() > tileX && controller.model.getTilesPerColumn() > tileY) {
            controller.tileImageInformations.setText(controller.model.getDstTileInformation(tileX, tileY));
        }
    }
}
