package de.tobiashh.javafx;

import javafx.scene.image.Image;

public class CalculationModel extends MosaikImageModelImpl {

    private Image sourceImage;

    public CalculationModel() {
        initChangeListener();
    }

    private void initChangeListener() {
        imageProperty().addListener((observable, oldImage, newImage) -> sourceImage = newImage);
    }
}
