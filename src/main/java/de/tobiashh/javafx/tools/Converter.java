package de.tobiashh.javafx.tools;

public class Converter {
    private final int tilesPerRow;

    public Converter(int tilesPerRow)
    {
        this.tilesPerRow = tilesPerRow;
    }

    public Position getPosition(int index){
        return new Position(index % tilesPerRow, index / tilesPerRow);
    }

    public int getIndex(Position position){
        return tilesPerRow * position.getY() + position.getX();
    }
}
