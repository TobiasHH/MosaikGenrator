package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.composer.impl.*;

public class ImageComposerFactory {

   public static ImageComposer getComposer(Mode mode){
      return switch (mode)
      {
         case LINEAR_NEW -> new LinearImageComposerNew();
         case RANDOM_NEW -> new RandomImageComposerNew();
         case CIRCULAR_NEW -> new CircleImageComposerNew();
         case TILE_DISTANCE_NEW -> new CenterImageComposerNew();
      };
   }
}