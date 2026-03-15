package com.ranoe.mineMine;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.joml.Vector2i;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import static com.ranoe.mineMine.util.MineUtils.spawnExplosion;

public class MineGame {
    Location origin;
    Sprite[][] field = new Sprite[16][16];
    boolean[][] revealed = new boolean[16][16];

    private static final Random random = new Random(42L);

    public MineGame(Location origin) {
        this.origin = origin;
    }

    public void init(Player player) {
        int x = ((origin.getBlockX() % 16) + 16) % 16;
        int z = ((origin.getBlockZ() % 16) + 16) % 16;
        // int y = origin.getY();
        int y = 128;

        field[x][z] = Sprite.UNKNOWN;
        naiveGeneration();

        for (int xi = 0; xi < 16; xi++) {
            for (int zi = 0; zi < 16; zi++) {
                Sprite sprite = Sprite.UNKNOWN;
                Block block = origin.getChunk().getBlock(xi, y, zi);
                block.setType(sprite.getMaterial());
                sprite.spawn(block.getLocation());
            }
        }

        player.teleport(origin.toCenterLocation().toHighestLocation().add(0, 4, 0));
    }

    public void naiveGeneration() {
        int size = field.length * field[0].length;
        int mines = field.length << 1;
        double chance = (double) mines / size;
        int placedMines = 0;

        while (placedMines < mines) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (field[x][z] != null) continue;
                    double r = (random.nextGaussian() / 2) + .5;
                    if (r < chance) {
                        field[x][z] = Sprite.BOMB;
                        placedMines++;
                    }
                }
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                revealed[x][z] = false;

                if (field[x][z] != Sprite.BOMB) {
                    AtomicInteger number = new AtomicInteger();
                    forEachNearby(x, z, (sprite, coords) -> {
                        if (sprite == Sprite.BOMB) {
                            number.getAndIncrement();
                        }
                    });
                    field[x][z] = Sprite.values()[number.get()];
                }
            }
        }
    }

    public void forEachNearby(int x, int z, BiConsumer<Sprite, Vector2i> action) {
        int minX = Math.max(x - 1, 0), maxX = Math.min(x + 1, 15);
        int minZ = Math.max(z - 1, 0), maxZ = Math.min(z + 1, 15);

        for (int xi = minX; xi <= maxX; xi++) {
            for (int zi = minZ; zi <= maxZ; zi++) {
                if (xi == x && zi == z) continue;
                action.accept(field[xi][zi], new Vector2i(xi, zi));
            }
        }
    }

    public void reveal(Block block) {
        int x = ((block.getX() % 16) + 16) % 16;
        int z = ((block.getZ() % 16) + 16) % 16;
        Sprite sprite = field[x][z];
        block.setType(sprite.getMaterial());
        sprite.spawn(block.getLocation());
        revealed[x][z] = true;

        if (sprite == Sprite.BOMB) {
            spawnExplosion(block);
        }
    }

    public void revealAll() {
        for (int xi = 0; xi < 16; xi++) {
            for (int zi = 0; zi < 16; zi++) {
                Block block = origin.getChunk().getBlock(xi, 128, zi);
                reveal(block);
            }
        }
    }

    public void flag(Block block) {
        Sprite.FLAG.spawn(block.getLocation());
        block.setType(Sprite.FLAG.getMaterial());
    }

    public void unflag(Block block) {
        Sprite.UNKNOWN.spawn(block.getLocation());
        block.setType(Sprite.UNKNOWN.getMaterial());
    }
}
