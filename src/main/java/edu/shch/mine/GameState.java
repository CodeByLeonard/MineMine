package edu.shch.mine;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class GameState {
    Player player;
    Block locator;

    ArrayList<BlockData> healCache;

    private GameState(Player player, Block locator) {
        this.player = player;
        this.locator = locator;
    }

    public void finish() {
        Chunk chunk = locator.getChunk();
        int height = locator.getY();
        int maxHeight = locator.getWorld().getMaxHeight();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.getBlock(x, height+1, z).setType(Material.AIR);
            }
        }

        for (int y = height; y < maxHeight; y++) {
            for (int x = 15; x >= 0; x--) {
                for (int z = 15; z >= 0; z--) {
                    chunk.getBlock(x, y, z).setBlockData(healCache.removeLast());
                }
            }
        }

        player.teleport(locator.getLocation().toCenterLocation());
        player.sendMessage("'grats!");
    }

    public static GameState from(Player player, Block block) {
        GameState state = new GameState(player, block);
        Chunk chunk = block.getChunk();
        state.healCache =  new ArrayList<>();

        int height = block.getY();
        int maxHeight = chunk.getWorld().getMaxHeight();

        int blockChunkX = ((block.getX() % 16) + 16) % 16;
        int blockChunkZ = ((block.getZ() % 16) + 16) % 16;
        System.out.println(chunk.getBlock(blockChunkX, height, blockChunkZ));

        // Retrieve Block Data for all Blocks in Chunk
        for (int y = maxHeight - 1; y >= height; y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockData data = chunk.getBlock(x, y, z).getBlockData();
                    if (x == blockChunkX && z == blockChunkZ && y <= height + 3) {
                        chunk.getBlock(x, y, z).setType(Material.AIR);
                        data = chunk.getBlock(x, y, z).getBlockData();
                    }
                    // TODO: (Re)store NBT as well, so that chests can be backed up
                    state.healCache.add(data);
                }
            }
        }

        // Void All Blocks (separate loop due to issues with leaves, snow, etc.)
        for (int y = maxHeight - 1; y >= height + 2; y--) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    chunk.getBlock(x, y, z).setType(Material.AIR);
                }
            }
        }

        // Place the Minesweeper Field
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.getBlock(x, height, z).setType(Material.SMOOTH_STONE);
                chunk.getBlock(x, height+1, z).setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
            }
        }

        return state;
    }
}
