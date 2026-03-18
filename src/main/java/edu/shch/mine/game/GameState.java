package edu.shch.mine.game;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.util.Vec2iComparator;
import edu.shch.mine.util.minecraft.ChunkUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import org.joml.Vector2i;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static edu.shch.mine.util.Utils.defer;

public class GameState {
    private final Player player;
    public final Block locator;
    private final int mines = 32;
    private BossBar bar;
    private int flagsPlaced = 0;

    private boolean finished = false;

    private final GameField[][] field = new GameField[16][16];
    private final FieldState[][] states = new FieldState[16][16];
    private final Consumer<GameState> finishCallback;

    private GameState(Player player, Block locator, Consumer<GameState> finishCallback) {
        this.player = player;
        this.locator = locator;
        this.finishCallback = finishCallback;
    }

    public void toggleFlag(int x, int z) {
        int blockChunkX = ((x % 16) + 16) % 16;
        int blockChunkZ = ((z % 16) + 16) % 16;
        int y = locator.getY() + 1;

        FieldState prior = states[blockChunkX][blockChunkZ];
        FieldState posterior = prior.getToggledFlagState();
        GameField cell = field[blockChunkX][blockChunkZ];
        Block block = locator.getChunk().getBlock(blockChunkX, y, blockChunkZ);

        if (prior == FieldState.FLAGGED) {
            MineSweeperPlugin.instance.getLogger().info("Unflagging Field...");
            flagsPlaced--;
        } else {
            MineSweeperPlugin.instance.getLogger().info("Flagging Field...");
            flagsPlaced++;
        }

        defer(() -> prior.changeTo(block.getLocation(), posterior, cell));
        states[blockChunkX][blockChunkZ] = posterior;
        this.bar.setProgress(1 - (double) flagsPlaced / mines);
    }

    public void uncover(Player player, int x, int z) {
        int blockChunkX = ((x % 16) + 16) % 16;
        int blockChunkZ = ((z % 16) + 16) % 16;
        int y = locator.getY();

        if (states[blockChunkX][blockChunkZ] == FieldState.FLAGGED) return;

        GameField gameField = field[blockChunkX][blockChunkZ];
        if (gameField == GameField.MINE) {
            finish(false);
            finishCallback.accept(this);
        } else {
            Block origin = locator.getChunk().getBlock(blockChunkX, y, blockChunkZ);
            if (gameField == GameField.NONE) {
                uncoverSurrounding(origin.getChunk(), blockChunkX, blockChunkZ, y);
            } else {
                FieldState.COVERED.changeTo(
                    origin.getRelative(BlockFace.UP).getLocation(),
                    FieldState.UNCOVERED,
                    gameField
                );
                states[blockChunkX][blockChunkZ] = FieldState.UNCOVERED;
            }

            boolean win = checkWinCondition();

            if (win) {
                finish(true);
                finishCallback.accept(this);
            } else {
                player.setVelocity(new Vector(0, 1f, 0));
            }
        }
    }

    private void uncoverSurrounding(Chunk chunk, int initialX, int initialZ, int y) {
        // Recursive Clearing
        TreeSet<Vector2i> cleared = new TreeSet<>(new Vec2iComparator());
        cleared.add(new Vector2i(initialX, initialZ));

        while (!cleared.isEmpty()) {
            Vector2i item = cleared.removeFirst();
            FieldState.COVERED.changeTo(
                chunk.getBlock(item.x, y + 1, item.y).getLocation(),
                FieldState.UNCOVERED,
                GameField.NONE
            );
            states[item.x][item.y] = FieldState.UNCOVERED;

            forEachSurrounding(field, item.x, item.y, (f, coords) -> {
                if (f == GameField.NONE && states[coords.x][coords.y] == FieldState.COVERED) {
                    cleared.add(coords);
                } else if (states[coords.x][coords.y] == FieldState.COVERED) {
                    FieldState.COVERED.changeTo(
                        chunk.getBlock(coords.x, y + 1, coords.y).getLocation(),
                        FieldState.UNCOVERED,
                        field[coords.x][coords.y]
                    );
                    states[coords.x][coords.y] = FieldState.UNCOVERED;
                }
            });
        }
    }

    @SuppressWarnings("OverlyComplexMethod")
    private boolean checkWinCondition() {
        boolean coveredNonMines = true;
        boolean flaggedMines = true;

        outer:
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                GameField field = this.field[x][z];
                FieldState state = this.states[x][z];
                if (coveredNonMines && field.isNonMine() && state != FieldState.UNCOVERED) {
                    coveredNonMines = false;
                }
                if (flaggedMines && field == GameField.MINE && state != FieldState.FLAGGED) {
                    flaggedMines = false;
                }
                if (!coveredNonMines && !flaggedMines) break outer;
            }
        }
        finished = coveredNonMines || flaggedMines;
        return finished;
    }

    private void finish(boolean win) {
        MineSweeperPlugin.instance.getLogger().info("Finished Game: %s".formatted(win));
        ChunkUtils.restoreChunk(locator.getChunk());
        player.teleport(locator.getLocation().toCenterLocation());
        defer(() -> {
            bar.removeAll();
            for (Entity entity : locator.getChunk().getEntities()) {
                if (entity instanceof ItemDisplay) {
                    entity.remove();
                }
            }

            if (win) {
                rewardPlayer();
            } else {
                punishPlayer();
            }
        });
    }

    public boolean isNotFinished() {
        return !finished;
    }

    private void punishPlayer() {
        player.getLocation().createExplosion(16f, false, false);
    }

    private void rewardPlayer() {
        player.getWorld().spawn(player.getLocation(), Firework.class, (firework) -> {
            FireworkMeta meta = firework.getFireworkMeta();
            FireworkEffect effect = FireworkEffect.builder()
                .withTrail().withColor(Color.RED)
                .withFlicker().withColor(Color.PURPLE)
                .withFade(Color.MAROON)
                .build();
            meta.setPower(8);
            meta.addEffect(effect);
            firework.setFireworkMeta(meta);
        });
    }

    public void revealMines(Player player) {
        Chunk chunk = locator.getChunk();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (field[x][z] == GameField.MINE) {
                    spawnMineParticle(player, chunk, x, z);
                }
            }
        }
    }

    private void spawnMineParticle(Player player, Chunk chunk, int x, int z) {
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

    private void coverMinesweeperField() {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                states[x][z] = FieldState.COVERED;
            }
        }
    }

    private void naiveGeneration() {
        double chance = this.mines / (16. * 16);

        Block block = this.locator;
        Chunk chunk = block.getChunk();
        int blockChunkX = ((block.getX() % 16) + 16) % 16;
        int blockChunkZ = ((block.getZ() % 16) + 16) % 16;
        int yLevel = block.getY();

        placeMinesRandomly(blockChunkX, blockChunkZ, chance);
        computeFieldNumbers(chunk, yLevel);
    }

    private void computeFieldNumbers(Chunk chunk, int yLevel) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                GameField field = this.field[x][z];
                if (field != GameField.MINE) {
                    int minesInVicinity = getMinesInVicinity(this.field, x, z);
                    this.field[x][z] = GameField.values()[minesInVicinity];
                }
                chunk.getBlock(x, yLevel, z).setType(GameField.NONE.type);
                chunk.getBlock(x, yLevel + 1, z).setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
            }
        }
    }

    private void placeMinesRandomly(int blockChunkX, int blockChunkZ, double chance) {
        int minesPlaced = 0;
        outer:
        while (minesPlaced < this.mines) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    GameField field = this.field[x][z];
                    if (field == GameField.MINE || (blockChunkX == x && blockChunkZ == z)) {
                        continue;
                    }
                    if (Math.random() < chance) {
                        this.field[x][z] = GameField.MINE;
                        minesPlaced++;
                    }

                    if (minesPlaced == this.mines) {
                        break outer;
                    }
                }
            }
        }
    }

    private void createProgressBarForPlayer(Player player) {
        this.bar = Bukkit.createBossBar("Mines", BarColor.RED, BarStyle.SEGMENTED_10, BarFlag.CREATE_FOG);
        this.bar.addPlayer(player);
    }

    public static GameState from(Player player, Block block, Consumer<GameState> finishCallback) {
        GameState state = new GameState(player, block, finishCallback);
        Chunk chunk = block.getChunk();

        int height = block.getY();
        int blockChunkX = ((block.getX() % 16) + 16) % 16;
        int blockChunkZ = ((block.getZ() % 16) + 16) % 16;

        for (int y = 2; y >= 0; y--) {
            chunk.getBlock(blockChunkX, height + y, blockChunkZ).setType(Material.AIR);
        }

        ChunkUtils.saveChunk(chunk, height);
        ChunkUtils.fillAir(chunk, height + 2);

        state.naiveGeneration();
        state.createProgressBarForPlayer(player);
        state.coverMinesweeperField();

        player.teleport(block.getLocation().toCenterLocation().add(0, 4, 0));
        return state;
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
}
