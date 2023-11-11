package de.tobiashh.javafx.tools;

public record Position(int x, int y) {

    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + '}';
    }

}

