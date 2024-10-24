package de.tobiashh.javafx;

import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CursorPositionEventHandler implements javafx.event.EventHandler<MouseEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CursorPositionEventHandler.class.getName());
    private final Controller controller;

    public CursorPositionEventHandler(Controller controller) {
        LOGGER.info("Erstelle CursorPositionEventHandler");
        this.controller = controller;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        controller.cursorPositionLabel.setText("x:" + (int) mouseEvent.getX() + " y:" + (int) mouseEvent.getY());
    }
}
