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
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class GameState {
    Player player;
    Block locator;
    BossBar bar;
    int mines = 32;
    int flagsPlaced = 0;

    ArrayList<BlockData> healCache;
    GameField[][] field = new GameField[16][16];
    FieldState[][] states = new FieldState[16][16];

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
            this.player.getServer().getScheduler().runTaskLater(
                MinePlugin.instance,
                () -> locator.getChunk().getBlock(blockChunkX, y, blockChunkZ)
                    .setType(GameField.UNKNOWN.block),
                1
            );
            flagsPlaced--;
        } else if (states[blockChunkX][blockChunkZ] == FieldState.COVERED) {
            states[blockChunkX][blockChunkZ] = FieldState.FLAGGED;
            this.player.getServer().getScheduler().runTaskLater(
                MinePlugin.instance,
                () -> locator.getChunk().getBlock(blockChunkX, y, blockChunkZ)
                    .setType(GameField.FLAG.block),
                1
            );
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
                    chunk.getBlock(item.x, y, item.y).setType(gameField.block);
                    forEachSurrounding(field, item.x, item.y, (f, coords) -> {
                        if (f == GameField.NONE && states[coords.x][coords.y] == FieldState.COVERED) {
                            cleared.add(coords);
                        } else if (states[coords.x][coords.y] == FieldState.COVERED) {
                            states[coords.x][coords.y] = FieldState.UNCOVERED;
                            chunk.getBlock(coords.x, y + 1, coords.y).setType(Material.AIR);
                            chunk.getBlock(coords.x, y, coords.y).setType(field[coords.x][coords.y].block);
                        }
                    });
                }
            } else {
                // Remove the Pressure Plate
                origin.getRelative(BlockFace.UP).setType(Material.AIR);
                origin.setType(gameField.block);
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
        boolean coveredNonMines = true;
        boolean flaggedMines = true;
        outerCheck:
        for (int xi = 0; xi < 16; xi++) {
            for (int zi = 0; zi < 16; zi++) {
                if (field[xi][zi].ordinal() <= GameField.EIGHT.ordinal() && states[xi][zi] != FieldState.UNCOVERED) {
                    coveredNonMines = false;
                }
                if (field[xi][zi] == GameField.MINE && states[xi][zi] != FieldState.FLAGGED) {
                    flaggedMines = false;
                }
                if (!coveredNonMines && !flaggedMines) {
                    break outerCheck;
                }
            }
        }
        boolean win = coveredNonMines || flaggedMines;
        return win;
    }

    public void finish(boolean win) {
        MinePlugin.instance.getLogger().info("Finished Game: %s".formatted(win));
        Chunk chunk = locator.getChunk();
        int height = locator.getY();
        int maxHeight = locator.getWorld().getMaxHeight();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.getBlock(x, height + 1, z).setType(Material.AIR);
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
        player.getServer().getScheduler().runTaskLater(MinePlugin.instance, () -> {
            bar.removeAll();
            if (win) {
                player.getWorld().spawn(player.getLocation(), Firework.class, (firework) -> {
                    FireworkMeta meta = firework.getFireworkMeta();
                    meta.addEffect(FireworkEffect.builder().trail(true).withColor(Color.RED).build());
                });
            } else {
                player.getLocation().createExplosion(12f, false, false);
            }
        }, 1);
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
        naiveGeneration(state);
        state.bar = Bukkit.createBossBar("Mines", BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);
        state.bar.addPlayer(player);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                state.states[x][z] = FieldState.COVERED;
            }
        }

        for (Player p : block.getLocation().getNearbyPlayers(48)) {
            p.teleport(block.getLocation().toCenterLocation().add(0, 4, 0));
            p.setFlying(true);

            // TODO: Construct Block Data that actually makes sense...
            // https://www.youtube.com/watch?v=K39U55l4-O0
            // https://minecraft.wiki/w/Pack_format
            // https://minecraft.wiki/w/Tutorial:Models#Item_predicates
            // TODO: Look into connecting textures?
            // BlockData ghostBlockData = Material.LAVA.createBlockData();
            // p.sendBlockChange(
            //         block.getRelative(0, 5, 0).getLocation(),
            //         ghostBlockData
            // );
            // Item Model Data: `/give @p red_banner[custom_model_data={strings:['flag']}]`
        }


        return state;
    }

    private static void naiveGeneration(GameState state) {
        int minesPlaced = 0;
        double chance = state.mines / (16. * 16);

        Block block = state.locator;
        Chunk chunk = block.getChunk();
        int blockChunkX = ((block.getX() % 16) + 16) % 16;
        int blockChunkZ = ((block.getZ() % 16) + 16) % 16;
        int yLevel = block.getY();

        outerLoop:
        while (minesPlaced < state.mines) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (state.field[x][z] == GameField.MINE || (blockChunkX == x && blockChunkZ == z)) {
                        continue;
                    }
                    if (Math.random() < chance) {
                        state.field[x][z] = GameField.MINE;
                        minesPlaced++;
                    }

                    if (minesPlaced == state.mines) break outerLoop;
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (state.field[x][z] != GameField.MINE) {
                    int minesInVicinity = getMinesInVicinity(state.field, x, z);
                    state.field[x][z] = GameField.values()[minesInVicinity];
                }

                // Material block = field[x][z].block;
                // NOTE: setType(block)
                chunk.getBlock(x, yLevel, z).setType(GameField.NONE.block);
                // NOTE: HEAVY_WEIGHTED_PRESSURE_PLATE / AIR
                chunk.getBlock(x, yLevel + 1, z).setType(GameField.UNKNOWN.block);
            }
        }
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

    private static void forEachSurrounding(GameField[][] field, int x, int z, BiConsumer<GameField, Vector2i> mapper) {
        int minX = Math.max(0, x - 1);
        int maxX = Math.min(x + 1, 16 - 1);
        int minZ = Math.max(0, z - 1);
        int maxZ = Math.min(z + 1, 16 - 1);
        for (int ix = minX; ix <= maxX; ix++) {
            for (int iz = minZ; iz <= maxZ; iz++) {
                mapper.accept(field[ix][iz], new Vector2i(ix, iz));
            }
        }
    }
}
