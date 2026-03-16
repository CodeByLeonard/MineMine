package edu.shch.mine;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.shch.mine.Utils.defer;

public class GameListener implements Listener {
    private static GameListener instance;

    private final ArrayList<GameState> games = new ArrayList<>();
    private final Map<Player, Boolean> xray = new HashMap<>();
    private final Map<Player, BukkitTask> playerTasks = new HashMap<>();

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
            int alphaPos = alphaIndex - index;
            int betaPos = betaIndex - index;

            Material alphaTestMaterial = event.getBlock().getRelative(0, alphaPos, 0).getType();
            Material betaTestMaterial = event.getBlock().getRelative(0, betaPos, 0).getType();

            Player player = event.getPlayer();
            if (alphaMaterial == alphaTestMaterial && betaMaterial == betaTestMaterial) {
                games.add(GameState.from(player, event.getBlock().getRelative(0, -index, 0)));
                Server server = player.getServer();
                defer(() -> {
                    List<Material> trash = List.of(
                        // Seeds
                        Material.WHEAT_SEEDS,
                        // Tree Drops
                        Material.SPRUCE_SAPLING,
                        // Flowers, Mushrooms
                        Material.POPPY
                    );
                    for (Entity e : server.selectEntities(server.getConsoleSender(), "@e[type=item]")) {
                        if (e instanceof Item item) {
                            if (trash.contains(item.getItemStack().getType())) {
                                item.remove();
                            }
                        }
                    }
                    player.getInventory().addItem(createFlag());
                });
            }
        }
    }

    @EventHandler
    public void checkMines(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL &&
            event.getClickedBlock() != null &&
            event.getClickedBlock().getType() == GameField.UNKNOWN.block
        ) {
            MinePlugin.instance.getLogger().info("Uncovering Field...");
            Block block = event.getClickedBlock();
            for (int i = 0; i < games.size(); i++) {
                GameState game = games.get(i);
                if (game.locator.getChunk().getChunkKey() == block.getChunk().getChunkKey()) {
                    Player player = event.getPlayer();
                    int gameIndex = i;
                    defer(() -> {
                        if (game.uncover(player, block.getX(), block.getZ())) {
                            games.remove(gameIndex);
                        }
                    });
                    break;
                }
            }
        }
    }

    @EventHandler
    public void toggleFlag(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().getType() != GameField.FLAG.block) return;
        Player player = event.getPlayer();
        for (int i = 0; i < games.size(); i++) {
            GameState game = games.get(i);
            if (game.locator.getChunk().getChunkKey() == player.getChunk().getChunkKey()) {
                RayTraceResult result = player.rayTraceBlocks(16);
                if (result == null) continue;
                Block block = result.getHitBlock();
                List<Material> flagBlocks = List.of(GameField.UNKNOWN.block, GameField.FLAG.block);
                if (block != null && flagBlocks.contains(block.getType())) {
                    defer(() -> {
                        if (game.toggleFlag(block.getX(), block.getZ())) {
                            games.remove(game);
                        } else {
                            player.getInventory().addItem(createFlag());
                            event.getItemDrop().remove();
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void equipGlasses(InventoryClickEvent event) {
        MinePlugin.instance.getLogger().info(event.toString());
        if (event.getInventory().getHolder() instanceof Player player) {
            defer(() -> checkHelmet(player));
        }
    }

    @EventHandler
    public void checkGlasses(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        defer(() -> checkHelmet(player));
    }

    private void checkHelmet(Player player) {
        @Nullable ItemStack helmet = player.getInventory().getHelmet();
        boolean enableHints = helmet != null && helmet.getType() == Material.COPPER_HELMET;
        xray.put(player, enableHints);

        if (!this.playerTasks.containsKey(player)) {
            this.playerTasks.put(player, Bukkit.getScheduler().runTaskTimer(
                MinePlugin.instance,
                () -> {
                    if (xray.get(player)) {
                        Chunk chunk = player.getChunk();
                        for (GameState game : games) {
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

    private static @NonNull ItemStack createFlag() {
        ItemStack flag = ItemStack.of(Material.RED_BANNER);
        ItemMeta itemMeta = flag.getItemMeta();
        //noinspection UnstableApiUsage
        CustomModelDataComponent cmd = itemMeta.getCustomModelDataComponent();
        //noinspection UnstableApiUsage
        cmd.setStrings(List.of("flag"));
        //noinspection UnstableApiUsage
        itemMeta.setCustomModelDataComponent(cmd);
        flag.setItemMeta(itemMeta);
        return flag.asOne();
    }

    public static GameListener getInstance() {
        if (instance == null) {
            instance = new GameListener();
        }
        return instance;
    }
}
