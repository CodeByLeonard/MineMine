package edu.shch.mine.game.event;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameField;
import edu.shch.mine.game.GameState;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import static edu.shch.mine.util.Utils.defer;

public class PlayerInteractHandler implements Listener {
    private static PlayerInteractHandler instance;
    private PlayerInteractHandler() {}

    @EventHandler
    public void checkMines(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL &&
            event.getClickedBlock() != null &&
            event.getClickedBlock().getType() == GameField.UNKNOWN.block
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

    public static PlayerInteractHandler getInstance() {
        if (instance == null) {
            instance = new PlayerInteractHandler();
        }
        return instance;
    }
}
