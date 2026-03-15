package edu.shch.mine;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.ArrayList;
import java.util.List;

public class GameListener implements Listener {
    private static GameListener instance;

    private final ArrayList<GameState> games = new ArrayList<>();

    private GameListener() {}

    @EventHandler
    public void initiateGame(BlockPlaceEvent event) {
        List<Material> blocks = List.of(
                Material.TNT,
                Material.SMOOTH_STONE,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE
        );

        Material block = event.getBlock().getType();
        if (blocks.contains(block)) {
            int index = blocks.indexOf(block);
            int alphaIndex = (index + 1) % blocks.size();
            int betaIndex = (index + 2) % blocks.size();
            Material alphaMaterial = blocks.get(alphaIndex);
            Material betaMaterial = blocks.get(betaIndex);
            int alphaPos =  alphaIndex - index;
            int betaPos = betaIndex - index;

            Material alphaTestMaterial = event.getBlock().getRelative(0, alphaPos, 0).getType();
            Material betaTestMaterial = event.getBlock().getRelative(0, betaPos, 0).getType();

            Player player = event.getPlayer();
            if (alphaMaterial == alphaTestMaterial && betaMaterial == betaTestMaterial) {
                games.add(GameState.from(player, event.getBlock().getRelative(0, -index, 0)));
            }

            player.getServer().getScheduler().runTaskLater(MinePlugin.instance, () -> {
                List<Material> trash = List.of(
                        // Seeds
                        Material.WHEAT_SEEDS,
                        // Tree Drops
                        Material.SPRUCE_SAPLING,
                        // Flowers
                        Material.POPPY
                );
                for (Entity e : player.getServer().selectEntities(player.getServer().getConsoleSender(), "@e[type=item]")) {
                    if (e instanceof Item item) {
                        if (trash.contains(item.getItemStack().getType())) {
                            item.remove();
                        }
                    }
                }
            }, 1);
        }
    }

    @EventHandler
    public void checkMines(BlockRedstoneEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            for (int i = 0; i < games.size(); i++) {
                GameState game = games.get(i);
                if (game.locator.getChunk().getChunkKey() == block.getChunk().getChunkKey()) {
                    List<Player> players = event.getBlock().getWorld().getPlayers();
                    if (!players.isEmpty()) {
                        Player player = players.getFirst();
                        Server server = player.getServer();
                        int gameIndex = i;
                        server.getScheduler().runTaskLater(
                                MinePlugin.instance,
                                () -> {
                                    if (game.uncover(block.getX(), block.getZ())) {
                                        games.remove(gameIndex);
                                    }
                                },
                                1
                        );
                    }
                    break;
                }
            }
        }
    }

    public static GameListener getInstance() {
        if (instance == null) {
            instance = new GameListener();
        }
        return instance;
    }
}
