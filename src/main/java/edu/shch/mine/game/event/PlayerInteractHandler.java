package edu.shch.mine.game.event;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static edu.shch.mine.util.Utils.defer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerInteractHandler implements Listener {
    @Getter(lazy = true)
    private static final PlayerInteractHandler instance = new PlayerInteractHandler();

    @EventHandler
    public void checkMines(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL &&
            event.getClickedBlock() != null &&
            event.getClickedBlock().getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE
        ) {
            MineSweeperPlugin plugin = MineSweeperPlugin.instance;
            plugin.getLogger().info("Uncovering Field...");
            Block block = event.getClickedBlock();
            for (GameState game : plugin.games) {
                if (game.locator.getChunk().getChunkKey() == block.getChunk().getChunkKey()) {
                    Player player = event.getPlayer();
                    defer(() -> game.uncover(player, block.getX(), block.getZ()));
                    break;
                }
            }
        }
    }
}
