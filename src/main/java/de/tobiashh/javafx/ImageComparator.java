package de.tobiashh.javafx;

import de.tobiashh.javafx.tiles.MosaikTile;
import de.tobiashh.javafx.tiles.Tile;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.ArrayList;
import java.util.List;

/*
 * gets a list of source tiles and destintion tiles
 * compare them to find the best match
 * does it in separat threads
 * save the scores in the source tile
 */
public class ImageComparator {
    ObservableList<MosaikTile> destinationTiles = FXCollections.observableList(new ArrayList<>());

    public MosaikTile compare(Tile tile){
        int bestScore = Integer.MAX_VALUE;
        MosaikTile retval = null;

        if(tile != null) {
            for (MosaikTile destinationTile : destinationTiles) {
               int score = tile.compare(destinationTile);
               if(score < bestScore){
                   bestScore = score;
                   retval = destinationTile;
               }
            }
        }

        return retval;
    }

    public void setMosaikTiles(List<MosaikTile> value) {
        destinationTiles.setAll(value);
    }
}
