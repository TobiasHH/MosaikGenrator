package de.tobiashh.javafx.model;

public class IndexConverter {
    private final int tilesPerRow;

    IndexConverter(int tilesPerRow)
    {
        this.tilesPerRow = tilesPerRow;
    }

    public Index2D convertLinearTo2D(int indexLinear){
        return new Index2D(indexLinear % tilesPerRow, indexLinear / tilesPerRow);
    }

    public int convert2DToLinear(Index2D index2D){
        return tilesPerRow * index2D.getY() + index2D.getX();
    }
}
