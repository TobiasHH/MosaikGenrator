package de.tobiashh.javafx;

import de.tobiashh.javafx.tools.Converter;
import de.tobiashh.javafx.tools.Position;
import javafx.event.Event;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DragEventHandler implements javafx.event.EventHandler<Event> {
    private final static Logger LOGGER = LoggerFactory.getLogger(DragEventHandler.class.getName());
    private final Controller controller;
    private Integer startTileX;
    private Integer startTileY;

    public DragEventHandler(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void handle(Event event) {
        if (controller.areaOfInterestCheck.isSelected()) {
            if (event.getEventType() == MouseEvent.DRAG_DETECTED) handleDragDetected((MouseEvent) event);
            if (event.getEventType() == MouseDragEvent.MOUSE_DRAG_RELEASED)
                handleMouseDragReleased((MouseDragEvent) event);
            event.consume();
        }
    }

    private void handleMouseDragReleased(MouseDragEvent mouseDragEvent) {
        int endTileX = controller.getTilePosition(mouseDragEvent.getX());
        int endTileY = controller.getTilePosition(mouseDragEvent.getY());
        for (int x = Math.min(startTileX, endTileX); x <= Math.max(startTileX, endTileX); x++) {
            for (int y = Math.min(startTileY, endTileY); y <= Math.max(startTileY, endTileY); y++) {
                if (mouseDragEvent.getButton().equals(MouseButton.PRIMARY)) {
                    controller.model.addAreaOfIntrest(x, y);
                    int index = new Converter(controller.model.tilesPerRowProperty().get()).getIndex(new Position(x, y));
                    controller.setTiles(controller.tiles.subList(index, index + 1));
                }
                if (mouseDragEvent.getButton().equals(MouseButton.SECONDARY)) {
                    controller.model.removeAreaOfIntrest(x, y);
                    int index = new Converter(controller.model.tilesPerRowProperty().get()).getIndex(new Position(x, y));
                    controller.setTiles(controller.tiles.subList(index, index + 1));
                }
            }
        }
    }

    private void handleDragDetected(MouseEvent mouseEvent) {
        Object source = mouseEvent.getSource();
        if (source instanceof ScrollPane) {
            LOGGER.info("DRAG_DETECTED");
            startTileX = controller.getTilePosition(mouseEvent.getX());
            startTileY = controller.getTilePosition(mouseEvent.getY());
            ((ScrollPane) source).startFullDrag();
        }
    }
}
