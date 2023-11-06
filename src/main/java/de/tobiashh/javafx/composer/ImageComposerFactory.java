package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.Mode;

public class ImageComposerFactory {

   public static ImageComposer getComposer(Mode mode){
      return switch (mode)
      {
         case LINEAR_NEW -> new LinearImageComposer();
         case RANDOM_NEW -> new RandomImageComposer();
         case CIRCULAR_NEW -> new CircleImageComposer();
         case TILE_DISTANCE_NEW -> new CenterImageComposer();
      };
   }
}