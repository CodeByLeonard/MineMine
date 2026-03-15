package edu.shch.mine;

import org.bukkit.Material;

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
    MINE(Material.REDSTONE_LAMP);

    final Material block;
    GameField(Material block) {
        this.block = block;
    }
}
