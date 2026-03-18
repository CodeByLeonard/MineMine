package edu.shch.mine.game;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.logic.FlagLogic;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public enum FieldState {
    COVERED,
    UNCOVERED,
    FLAGGED;

    @Getter(lazy = true)
    private static final ItemStack flag = FlagLogic.createFlag();

    public void changeTo(Location location, FieldState state, GameField field) {
        if (this == COVERED) {
            if (state == UNCOVERED) {
                assert (field.isNonMine());
                Block top = location.getBlock();
                top.setType(Material.AIR);

                Block bottom = top.getRelative(BlockFace.DOWN);
                if (field != GameField.NONE) {
                    bottom.setType(Material.BARRIER);
                    field.spawnItemDisplay(bottom.getLocation());
                } else {
                    bottom.setType(GameField.NONE.type);
                }
            } else if (state == FLAGGED) {
                flag(location);
            }
        } else if (this == FLAGGED && state == COVERED) {
            unflag(location);
        }
    }

    public FieldState getToggledFlagState() {
        return switch (this) {
            case COVERED -> FLAGGED;
            case FLAGGED -> COVERED;
            case UNCOVERED -> null;
        };
    }

    private static void unflag(Location location) {
        Collection<ItemDisplay> foundEntities = location.toCenterLocation()
            .getNearbyEntitiesByType(ItemDisplay.class, .1);
        for (ItemDisplay display : foundEntities) {
            if (display.getItemStack().equals(getFlag())) {
                display.remove();
            } else {
                //noinspection UnstableApiUsage
                MineSweeperPlugin.instance.getLogger()
                    .warning("Anomalous ItemDisplay Detected (Flagged -> Covered): " +
                        display.getAsString());
            }
        }
        location.getBlock().setType(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
    }

    private static void flag(Location location) {
        location.getBlock().setType(Material.AIR);
        location.getWorld().spawn(
            location.toCenterLocation(),
            ItemDisplay.class,
            entity -> entity.setItemStack(getFlag()));
    }
}
