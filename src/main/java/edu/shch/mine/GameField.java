package edu.shch.mine;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

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

    final Material block;
    GameField(Material block) {
        this.block = block;
    }

    @SuppressWarnings("UnstableApiUsage")
    ItemStack getEntityStack() {
        ItemStack stack = ItemStack.of(this.block);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of("mine"));
        meta.setCustomModelDataComponent(cmd);
        stack.setItemMeta(meta);
        return stack;
    }

    ItemDisplay spawnItemDisplay(Block block) {
        return block.getWorld().spawn(
            block.getLocation().toCenterLocation(),
            ItemDisplay.class,
            entity -> entity.setItemStack(getEntityStack())
        );
    }
}
