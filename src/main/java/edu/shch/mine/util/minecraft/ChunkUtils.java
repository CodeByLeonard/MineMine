package edu.shch.mine.util.minecraft;

import edu.shch.mine.util.Pair;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class ChunkUtils {
    private final HashMap<Long, Pair<Integer, ArrayList<BlockData>>> healCache = new HashMap<>();

    private static ChunkUtils instance;
    private ChunkUtils() {}

    public void saveChunk(Chunk chunk, int height) {
        ArrayList<BlockData> data = new ArrayList<>();

        // Iterate Top to Bottom in Reverse: Bottom-Up
        // Helps with Grass/Flowers (require block below),
        // but potentially causes problems with Vines
        // (hanging blocks, require block above), etc.
        int maxHeight = chunk.getWorld().getMaxHeight();
        for (int y = maxHeight - 1; y >= height; y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    data.add(chunk.getBlock(x, y, z).getBlockData());
                }
            }
        }

        //noinspection SuspiciousNameCombination
        healCache.put(chunk.getChunkKey(), Pair.of(height, data));
    }

    public void restoreChunk(Chunk chunk) {
        long key = chunk.getChunkKey();
        if (!healCache.containsKey(key)) return;
        Pair<Integer, ArrayList<BlockData>> store = healCache.get(key);
        int height = store.x();
        ArrayList<BlockData> data = store.y();

        int layers = data.size() / (16 * 16);
        int index = data.size() - 1;
        for (int y = height; y < height + layers; y++) {
            for (int x = 15; x >= 0; x--) {
                for (int z = 15; z >= 0; z--) {
                    BlockData datum = data.get(index);
                    chunk.getBlock(x, y, z).setBlockData(datum, false);
                    index--;
                }
            }
        }
        healCache.remove(key);
    }

    public void fillAir(Chunk chunk, int from) {
        int maxHeight = chunk.getWorld().getMaxHeight();
        for (int y = maxHeight - 1; y >= from; y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    chunk.getBlock(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    public boolean sameChunk(Player player, Block block) {
        return player.getChunk().getChunkKey() == block.getChunk().getChunkKey();
    }

    public boolean sameChunk(Block a, Block b) {
        return a.getChunk().getChunkKey() == b.getChunk().getChunkKey();
    }

    public static ChunkUtils getInstance() {
        if (instance == null) {
            instance = new ChunkUtils();
        }
        return instance;
    }
}
