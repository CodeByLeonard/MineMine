package edu.shch.mine.game.event;

import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collection;

public class BlockBreakHandler implements Listener {
    private static BlockBreakHandler instance;
    private BlockBreakHandler() {}

    @EventHandler
    public void destroyEntityOnBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BARRIER) {
            Collection<ItemDisplay> displays = event.getBlock().getLocation()
                .toCenterLocation().getNearbyEntitiesByType(ItemDisplay.class, .1);
            for (ItemDisplay display : displays) {
                display.remove();
            }
        }
    }

    public static BlockBreakHandler getInstance() {
        if (instance == null) {
            instance = new BlockBreakHandler();
        }
        return instance;
    }
}
