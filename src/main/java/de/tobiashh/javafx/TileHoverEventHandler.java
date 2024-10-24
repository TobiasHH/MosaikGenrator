package de.tobiashh.javafx;

import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TileHoverEventHandler implements javafx.event.EventHandler<MouseEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(TileHoverEventHandler.class.getName());
    private final Controller controller;

    public TileHoverEventHandler(Controller controller) {
        LOGGER.info("Erstelle TileHoverEventHandler");
        this.controller = controller;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        int tileX = controller.getTilePosition(mouseEvent.getX());
        int tileY = controller.getTilePosition(mouseEvent.getY());
        controller.tileHoverLabel.setText("x:" + tileX + " y:" + tileY);
    }
}
