package de.tobiashh.javafx.tools;

public class IndexConverter {
    private final int tilesPerRow;

    public IndexConverter(int tilesPerRow)
    {
        this.tilesPerRow = tilesPerRow;
    }

    public Position convertLinearTo2D(int indexLinear){
        return new Position(indexLinear % tilesPerRow, indexLinear / tilesPerRow);
    }

    public int convert2DToLinear(Position index2D){
        return tilesPerRow * index2D.getY() + index2D.getX();
    }
}
