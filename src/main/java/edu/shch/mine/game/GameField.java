package edu.shch.mine.game;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.*;

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

    private static final Map<Material, GameField> MATERIAL_INDEX = Arrays.stream(GameField.values())
        .collect(HashMap::new, (map, field) -> map.put(field.type, field), HashMap::putAll);

    public final Material type;
    GameField(Material type) {
        this.type = type;
    }

    @SuppressWarnings("UnstableApiUsage")
    ItemStack getEntityStack() {
        ItemStack stack = ItemStack.of(this.type);
        ItemMeta meta = stack.getItemMeta();
        CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
        cmd.setStrings(List.of("mine"));
        meta.setCustomModelDataComponent(cmd);
        stack.setItemMeta(meta);
        return stack;
    }

    public void spawnItemDisplay(Location location) {
        location.getWorld().spawn(
            location.toCenterLocation(),
            ItemDisplay.class,
            entity -> entity.setItemStack(getEntityStack())
        );
    }

    boolean isNonMine() {
        return this != MINE;
    }

    public static Optional<GameField> fromMaterial(Material material) {
        return Optional.ofNullable(MATERIAL_INDEX.get(material));
    }

    public static final List<Material> NON_MINE_MATERIALS = Arrays.stream(GameField.values())
        .skip(1)
        .takeWhile(f -> f.ordinal() <= 8)
        .map(f -> f.type)
        .toList();
}
