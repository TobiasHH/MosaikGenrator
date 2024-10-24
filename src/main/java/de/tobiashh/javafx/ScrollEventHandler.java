package de.tobiashh.javafx;

import javafx.scene.input.ScrollEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScrollEventHandler implements javafx.event.EventHandler<ScrollEvent> {
    private final static Logger LOGGER = LoggerFactory.getLogger(ScrollEventHandler.class.getName());
    private final Controller controller;

    public ScrollEventHandler(Controller controller) {
        LOGGER.info("Erstelle ScrollEventHandler");
        this.controller = controller;
    }

    @Override
    public void handle(ScrollEvent scrollEvent) {
        if (scrollEvent.isControlDown()) {
            controller.setScale(controller.getScale() * (1 + scrollEvent.getDeltaY() / 100));
            scrollEvent.consume();
        }
    }
}
