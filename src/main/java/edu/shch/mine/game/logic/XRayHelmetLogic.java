package edu.shch.mine.game.logic;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class XRayHelmetLogic {
    private final Map<Player, Boolean> xray = new HashMap<>();
    private final Map<Player, BukkitTask> playerTasks = new HashMap<>();
    private static XRayHelmetLogic instance;

    public void checkHelmet(Player player) {
        @Nullable ItemStack helmet = player.getInventory().getHelmet();
        boolean enableHints = helmet != null && helmet.getType() == Material.COPPER_HELMET;
        xray.put(player, enableHints);

        if (!this.playerTasks.containsKey(player)) {
            MineSweeperPlugin plugin = MineSweeperPlugin.instance;

            this.playerTasks.put(player, Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    if (xray.get(player)) {
                        Chunk chunk = player.getChunk();
                        for (GameState game : plugin.games) {
                            if (game.locator.getChunk().getChunkKey() == chunk.getChunkKey()) {
                                game.revealMines(player);
                            }
                        }
                    }
                },
                0,
                20
            ));
        }
    }

    public static XRayHelmetLogic getInstance() {
        if (instance == null) {
            instance = new XRayHelmetLogic();
        }
        return instance;
    }
}
