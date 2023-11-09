package de.tobiashh.javafx.model;

import de.tobiashh.javafx.tiles.DstTile;
import de.tobiashh.javafx.tiles.OriginalTile;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CompareTask extends Task<List<List<Integer>>> {
    private final static Logger LOGGER = LoggerFactory.getLogger(CompareTask.class.getName());
    private final MosaikImage mosaikImage;
    private final List<DstTile> dstTilesList;
    private final int preColorAlignment;

    public CompareTask(
            MosaikImage mosaikImage,
            List<DstTile> dstTilesList,
            int preColorAlignment
    ) {
        this.mosaikImage = mosaikImage;
        this.dstTilesList = dstTilesList;
        this.preColorAlignment = preColorAlignment;
    }

    @Override
    protected List<List<Integer>> call() {
        LOGGER.info("CompareTask start");
        List<List<Integer>> retval = compareTiles(Arrays.asList(mosaikImage.getTiles()), dstTilesList);

        LOGGER.info("CompareTask finished");
        return retval;
    }

    private List<List<Integer>> compareTiles(List<OriginalTile> mosaicImage, List<DstTile> dstTilesList) {
        List<List<Integer>> scoredDstTileLists = new ArrayList<>();
        for (int x = 0; x < mosaicImage.size(); x++) {
            scoredDstTileLists.add(new ArrayList<>());
        }

        IntStream.range(0, mosaicImage.size()).parallel().forEach(mosaikImageIndex -> {
            Map<Integer, Integer> scores = new HashMap<>();

            for (int index = 0; index < dstTilesList.size(); index++) {
                scores.put(index, mosaicImage.get(mosaikImageIndex).compare(dstTilesList.get(index), preColorAlignment));
            }

            scoredDstTileLists.get(mosaikImageIndex).clear();
            scoredDstTileLists.get(mosaikImageIndex).addAll(getIndexSortedByScore(scores));
        });

        checkIntegrity(dstTilesList, scoredDstTileLists);
        return scoredDstTileLists;
    }

    protected List<Integer> getIndexSortedByScore(Map<Integer, Integer> scores) {
        List<Map.Entry<Integer, Integer>> list = new ArrayList<>(scores.entrySet());
        list.sort(Map.Entry.comparingByValue());
        return list.stream().mapToInt(Map.Entry::getKey).boxed().collect(Collectors.toList());
    }

    private void checkIntegrity(List<DstTile> dstTilesList, List<List<Integer>> scoredDstTileLists) {
        for (List<Integer> scoredDstTileList : scoredDstTileLists) {
            if (dstTilesList.size() != scoredDstTileList.size()) {
                LOGGER.warn("scored list not correct size");
                System.exit(-1);
            }
        }
    }
}
