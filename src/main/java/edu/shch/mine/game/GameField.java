package edu.shch.mine.game;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum GameField {
    NONE(Material.SMOOTH_STONE),
    ONE(Material.BLUE_CONCRETE),
    TWO(Material.GREEN_CONCRETE),
    THREE(Material.RED_CONCRETE),
    FOUR(Material.PURPLE_CONCRETE),
    FIVE(Material.BROWN_CONCRETE),
    SIX(Material.CYAN_CONCRETE),
    SEVEN(Material.GRAY_CONCRETE),
    EIGHT(Material.BLACK_CONCRETE),
    MINE(Material.REDSTONE_LAMP),
    UNKNOWN(Material.HEAVY_WEIGHTED_PRESSURE_PLATE),
    FLAG(Material.RED_BANNER);

    public final Material block;
    GameField(Material block) {
        this.block = block;
    }

    @SuppressWarnings("UnstableApiUsage")
    ItemStack getEntityStack() {
        ItemStack stack = ItemStack.of(this.block);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        if (this.ordinal() <= 8) {
            cmd.setStrings(List.of("mine"));
        } else {
            cmd.setStrings(List.of("flag"));
        }
        meta.setCustomModelDataComponent(cmd);
        stack.setItemMeta(meta);
        return stack;
    }

    public ItemDisplay spawnItemDisplay(Block block) {
        return block.getWorld().spawn(
            block.getLocation().toCenterLocation(),
            ItemDisplay.class,
            entity -> entity.setItemStack(getEntityStack())
        );
    }

    boolean isNonMine() {
        return this.ordinal() <= EIGHT.ordinal();
    }

    public static Optional<GameField> fromMaterial(Material material) {
        for (GameField field : GameField.values()) {
            if (field.block == material) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    public static final List<Material> NON_MINE_MATERIALS = Arrays.stream(GameField.values())
        .skip(1)
        .takeWhile(f -> f.ordinal() <= 8)
        .map(f -> f.block)
        .toList();
}
