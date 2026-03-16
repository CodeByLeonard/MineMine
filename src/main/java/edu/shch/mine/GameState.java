package edu.shch.mine;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static edu.shch.mine.Utils.defer;

public class GameState {
    Player player;
    Block locator;
    BossBar bar;
    int mines = 32;
    int flagsPlaced = 0;

    ArrayList<BlockData> healCache;
    GameField[][] field = new GameField[16][16];
    FieldState[][] states = new FieldState[16][16];
    List<ItemDisplay> entities = new ArrayList<>();

    private GameState(Player player, Block locator) {
        this.player = player;
        this.locator = locator;
    }

    public boolean toggleFlag(int x, int z) {
        int blockChunkX = ((x % 16) + 16) % 16;
        int blockChunkZ = ((z % 16) + 16) % 16;
        int y = locator.getY() + 1;

        if (states[blockChunkX][blockChunkZ] == FieldState.FLAGGED) {
            states[blockChunkX][blockChunkZ] = FieldState.COVERED;
            defer(() -> locator.getChunk().getBlock(blockChunkX, y, blockChunkZ)
                .setType(GameField.UNKNOWN.block));
            flagsPlaced--;
        } else if (states[blockChunkX][blockChunkZ] == FieldState.COVERED) {
            states[blockChunkX][blockChunkZ] = FieldState.FLAGGED;
            defer(() -> locator.getChunk().getBlock(blockChunkX, y, blockChunkZ)
                .setType(GameField.FLAG.block));
            flagsPlaced++;

            if (flagsPlaced == mines && checkWinCondition()) {
                finish(true);
                return true;
            }
        }

        this.bar.setProgress(1 - (double) flagsPlaced / mines);
        return false;
    }

    public boolean uncover(Player player, int x, int z) {
        int blockChunkX = ((x % 16) + 16) % 16;
        int blockChunkZ = ((z % 16) + 16) % 16;
        int y = locator.getY();

        if (states[blockChunkX][blockChunkZ] == FieldState.FLAGGED) return false;

        GameField gameField = field[blockChunkX][blockChunkZ];
        if (gameField == GameField.MINE) {
            finish(false);
            return true;
        } else {
            Block origin = locator.getChunk().getBlock(blockChunkX, y, blockChunkZ);
            // Mark Block
            if (gameField == GameField.NONE) {
                Chunk chunk = origin.getChunk();
                // Recursive Clearing
                List<Vector2i> cleared = new ArrayList<>();
                cleared.add(new Vector2i(blockChunkX, blockChunkZ));

                while (!cleared.isEmpty()) {
                    Vector2i item = cleared.removeFirst();
                    states[item.x][item.y] = FieldState.UNCOVERED;
                    chunk.getBlock(item.x, y + 1, item.y).setType(Material.AIR);
                    Block block = chunk.getBlock(item.x, y, item.y);
                    block.setType(Material.BARRIER);
                    entities.add(gameField.spawnItemDisplay(block));
                    forEachSurrounding(field, item.x, item.y, (f, coords) -> {
                        if (f == GameField.NONE && states[coords.x][coords.y] == FieldState.COVERED) {
                            cleared.add(coords);
                        } else if (states[coords.x][coords.y] == FieldState.COVERED) {
                            states[coords.x][coords.y] = FieldState.UNCOVERED;
                            chunk.getBlock(coords.x, y + 1, coords.y).setType(Material.AIR);
                            Block recBlock = chunk.getBlock(coords.x, y, coords.y);
                            GameField recField = field[coords.x][coords.y];
                            recBlock.setType(Material.BARRIER);
                            entities.add(recField.spawnItemDisplay(recBlock));
                        }
                    });
                }
            } else {
                // Remove the Pressure Plate
                origin.getRelative(BlockFace.UP).setType(Material.AIR);
                origin.setType(Material.BARRIER);
                entities.add(gameField.spawnItemDisplay(origin));
                states[blockChunkX][blockChunkZ] = FieldState.UNCOVERED;
            }

            boolean win = checkWinCondition();

            if (win) {
                finish(true);
                return true;
            } else {
                player.setVelocity(new Vector(0, 1f, 0));
            }
        }
        return false;
    }

    private boolean checkWinCondition() {
        AtomicBoolean coveredNonMines = new AtomicBoolean(true);
        AtomicBoolean flaggedMines = new AtomicBoolean(true);
        forEach(field, (field, coords) -> {
            if (field.ordinal() <= GameField.EIGHT.ordinal() && states[coords.x][coords.y] != FieldState.UNCOVERED) {
                coveredNonMines.set(false);
            }
            if (field == GameField.MINE && states[coords.x][coords.y] != FieldState.FLAGGED) {
                flaggedMines.set(false);
            }
            return coveredNonMines.get() || flaggedMines.get();
        });
        return coveredNonMines.get() || flaggedMines.get();
    }

    public void finish(boolean win) {
        MinePlugin.instance.getLogger().info("Finished Game: %s".formatted(win));
        Chunk chunk = locator.getChunk();
        int height = locator.getY();
        int maxHeight = locator.getWorld().getMaxHeight();
        forEach(field, (_, coords) -> {
            chunk.getBlock(coords.x, height + 1, coords.y).setType(Material.AIR);
            return true;
        });

        for (int y = height; y < maxHeight; y++) {
            for (int x = 15; x >= 0; x--) {
                for (int z = 15; z >= 0; z--) {
                    chunk.getBlock(x, y, z).setBlockData(healCache.removeLast());
                }
            }
        }

        player.teleport(locator.getLocation().toCenterLocation());
        defer(() -> {
            bar.removeAll();

            for (ItemDisplay entity : entities) {
                entity.remove();
            }

            if (win) {
                player.getWorld().spawn(player.getLocation(), Firework.class, (firework) -> {
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder().trail(true).withColor(Color.RED).build());
                });
            } else {
                player.getLocation().createExplosion(12f, false, false);
            }
        });
    }

    public void revealMines(Player player) {
        Chunk chunk = locator.getChunk();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (field[x][z] == GameField.MINE) {
                    Particle.SOUL_FIRE_FLAME.builder()
                        .location(
                            chunk.getBlock(x, locator.getY(), z)
                                .getRelative(0, 2, 0)
                                .getLocation().toCenterLocation()
                        )
                        .offset(0, .2, 0)
                        .count(0)
                        .extra(0.1)
                        .receivers(player)
                        .spawn();
                }
            }
        }
    }

    public static GameState from(Player player, Block block) {
        GameState state = new GameState(player, block);
        Chunk chunk = block.getChunk();
        state.healCache = new ArrayList<>();

        int height = block.getY();
        int maxHeight = chunk.getWorld().getMaxHeight();

        int blockChunkX = ((block.getX() % 16) + 16) % 16;
        int blockChunkZ = ((block.getZ() % 16) + 16) % 16;
        System.out.println(chunk.getBlock(blockChunkX, height, blockChunkZ));

        // Retrieve Block Data for all Blocks in Chunk
        for (int y = maxHeight - 1; y >= height; y--) {
            int currentHeight = y;
            forEach(state.field, (_, coords) -> {
                BlockData data = chunk.getBlock(coords.x, currentHeight, coords.y).getBlockData();
                if (coords.x == blockChunkX && coords.y == blockChunkZ && currentHeight <= height + 3) {
                    chunk.getBlock(coords.x, currentHeight, coords.y).setType(Material.AIR);
                    data = chunk.getBlock(coords.x, currentHeight, coords.y).getBlockData();
                }
                // TODO: (Re)store NBT as well, so that chests can be backed up
                state.healCache.add(data);
                return true;
            });
        }

        // Void All Blocks (separate loop due to issues with leaves, snow, etc.)
        for (int y = maxHeight - 1; y >= height + 2; y--) {
            int currentY = y;
            forEach(state.field, (_, coords) -> {
                chunk.getBlock(coords.x, currentY, coords.y).setType(Material.AIR);
                return true;
            });
        }

        // Place the Minesweeper Field
        naiveGeneration(state);
        state.bar = Bukkit.createBossBar("Mines", BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);
        state.bar.addPlayer(player);

        forEach(state.states, (_, coords) -> {
            state.states[coords.x][coords.y] = FieldState.COVERED;
            return true;
        });

        player.teleport(block.getLocation().toCenterLocation().add(0, 4, 0));
        return state;
    }

    private static void naiveGeneration(GameState state) {
        AtomicInteger minesPlaced = new AtomicInteger();
        double chance = state.mines / (16. * 16);

        Block block = state.locator;
        Chunk chunk = block.getChunk();
        int blockChunkX = ((block.getX() % 16) + 16) % 16;
        int blockChunkZ = ((block.getZ() % 16) + 16) % 16;
        int yLevel = block.getY();

        while (minesPlaced.get() < state.mines) {
            forEach(state.field, (f, coords) -> {
                if (f == GameField.MINE || (blockChunkX == coords.x && blockChunkZ == coords.y)) {
                    return true;
                }
                if (Math.random() < chance) {
                    state.field[coords.x][coords.y] = GameField.MINE;
                    minesPlaced.getAndIncrement();
                }

                return minesPlaced.get() != state.mines;
            });
        }

        forEach(state.field, (f, coords) -> {
            if (f != GameField.MINE) {
                int minesInVicinity = getMinesInVicinity(state.field, coords.x, coords.y);
                state.field[coords.x][coords.y] = GameField.values()[minesInVicinity];
            }
            chunk.getBlock(coords.x, yLevel, coords.y).setType(GameField.NONE.block);
            chunk.getBlock(coords.x, yLevel + 1, coords.y).setType(GameField.UNKNOWN.block);
            return true;
        });
    }

    private static int getMinesInVicinity(GameField[][] field, int x, int z) {
        AtomicInteger minesInVicinity = new AtomicInteger(0);
        forEachSurrounding(field, x, z, (f, _) -> {
            if (f == GameField.MINE) {
                minesInVicinity.incrementAndGet();
            }
        });
        return minesInVicinity.get();
    }

    private static void forEachSurrounding(GameField[][] field, int x, int z, BiConsumer<GameField, Vector2i> action) {
        int minX = Math.max(0, x - 1);
        int maxX = Math.min(x + 1, 16 - 1);
        int minZ = Math.max(0, z - 1);
        int maxZ = Math.min(z + 1, 16 - 1);
        for (int ix = minX; ix <= maxX; ix++) {
            for (int iz = minZ; iz <= maxZ; iz++) {
                action.accept(field[ix][iz], new Vector2i(ix, iz));
            }
        }
    }

    private static <T> void forEach(T[][] field, BiFunction<T, Vector2i, Boolean> action) {
        boolean resume = true;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                resume = action.apply(field[x][z], new Vector2i(x, z));
                if (!resume) break;
            }
            if (!resume) break;
        }
    }
}
