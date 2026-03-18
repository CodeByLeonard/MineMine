package edu.shch.mine.game.event;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameField;
import edu.shch.mine.game.GameState;
import edu.shch.mine.game.logic.FlagLogic;
import edu.shch.mine.util.minecraft.ChunkUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerDropItemHandler implements Listener {
    @Getter(lazy = true)
    private static final PlayerDropItemHandler instance = new PlayerDropItemHandler();

    @EventHandler
    public void toggleFlag(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != Material.RED_BANNER) return;

        MineSweeperPlugin plugin = MineSweeperPlugin.instance;
        ArrayList<GameState> games = plugin.games;
        Player player = event.getPlayer();

        for (GameState game : games) {
            if (!ChunkUtils.sameChunk(player, game.locator)) continue;

            RayTraceResult result = player.rayTraceBlocks(16);
            if (result == null) continue;

            Block block = result.getHitBlock();
            if (block == null) continue;

            if (block.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                FlagLogic.toggleFlag(game, block, () -> event.setCancelled(true));
            } else if (block.getType() == GameField.NONE.type) {
                FlagLogic.unflag(game, block, () -> event.setCancelled(true));
            }
        }
    }
}
