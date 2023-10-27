package de.tobiashh.javafx.tools;

public class Index2D {
    private final int x;
    private final int y;

    public Index2D(int x, int y){
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return "Index2D{x=" + x + ", y=" + y + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Index2D index2D = (Index2D) o;
        return x == index2D.x && y == index2D.y;
    }
}

