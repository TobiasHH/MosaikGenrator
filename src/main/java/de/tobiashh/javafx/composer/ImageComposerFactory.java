package de.tobiashh.javafx.composer;

import de.tobiashh.javafx.Mode;
import de.tobiashh.javafx.composer.impl.CenterImageComposer;
import de.tobiashh.javafx.composer.impl.CircleImageComposer;
import de.tobiashh.javafx.composer.impl.LinearImageComposer;
import de.tobiashh.javafx.composer.impl.RandomImageComposer;

public class ImageComposerFactory {

   public static ImageComposer getComposer(Mode mode){
      return switch (mode)
      {
         case LINEAR -> new LinearImageComposer();
         case RANDOM -> new RandomImageComposer();
         case CIRCULAR -> new CircleImageComposer();
         case TILE_DISTANCE -> new CenterImageComposer();
      };
   }
}