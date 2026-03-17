package edu.shch.mine.game.event;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameField;
import edu.shch.mine.game.GameState;
import edu.shch.mine.game.logic.FlagLogic;
import edu.shch.mine.util.minecraft.ChunkUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;

public class PlayerDropItemHandler implements Listener {
    private static PlayerDropItemHandler instance;
    private PlayerDropItemHandler() {}

    @EventHandler
    public void toggleFlag(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != GameField.FLAG.block) return;

        MineSweeperPlugin plugin = MineSweeperPlugin.instance;
        ArrayList<GameState> games = plugin.games;
        Player player = event.getPlayer();
        ChunkUtils utils = ChunkUtils.getInstance();
        FlagLogic logic = FlagLogic.getInstance();

        for (GameState game : games) {
            if (!utils.sameChunk(player, game.locator)) continue;

            RayTraceResult result = player.rayTraceBlocks(16);
            if (result == null) continue;

            Block block = result.getHitBlock();
            if (block == null) continue;

            if (block.getType() == GameField.UNKNOWN.block) {
                logic.toggleFlag(event.getItemDrop(), game, block, player.getInventory());
            } else if (block.getType() == GameField.NONE.block) {
                logic.unflag(event.getItemDrop(), game, block, player.getInventory());
            }
        }
    }

    public static PlayerDropItemHandler getInstance() {
        if (instance == null) {
            instance = new PlayerDropItemHandler();
        }
        return instance;
    }
}
