package edu.shch.mine.game.event;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameState;
import edu.shch.mine.util.minecraft.ChunkUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collection;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockBreakHandler implements Listener {
    @Getter(lazy = true)
    private static final BlockBreakHandler instance = new BlockBreakHandler();

    @EventHandler
    public void handleBreak(BlockBreakEvent event) {
        for (GameState game : MineSweeperPlugin.instance.games) {
            if (ChunkUtils.sameChunk(event.getBlock(), game.locator)) {
                event.setCancelled(true);
                break;
            }
        }
        destroyEntityOnBreak(event);
    }

    private static void destroyEntityOnBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.BARRIER) {
            Collection<ItemDisplay> displays = event.getBlock().getLocation()
                .toCenterLocation().getNearbyEntitiesByType(ItemDisplay.class, .1);
            for (ItemDisplay display : displays) {
                display.remove();
            }
        }
    }
}
