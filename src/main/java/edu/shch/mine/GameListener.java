package edu.shch.mine;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
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

import java.util.*;

import static edu.shch.mine.Utils.defer;

public class GameListener implements Listener {
    private static GameListener instance;

    private final ArrayList<GameState> games = new ArrayList<>();
    private final Map<Player, Boolean> xray = new HashMap<>();
    private final Map<Player, BukkitTask> playerTasks = new HashMap<>();

    private static final List<Material> gameStartBlocks = List.of(
        Material.TNT,
        Material.SMOOTH_STONE,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE
    );

    private static final List<Material> fieldBlocks = Arrays.stream(GameField.values())
        .skip(1)
        .takeWhile(f -> f.ordinal() <= 8)
        .map(f -> f.block)
        .toList();

    private GameListener() {}

    @EventHandler
    public void initiateGame(BlockPlaceEvent event) {
        Material block = event.getBlock().getType();
        if (gameStartBlocks.contains(block)) {
            int index = gameStartBlocks.indexOf(block);
            int alphaIndex = (index + 1) % gameStartBlocks.size();
            int betaIndex = (index + 2) % gameStartBlocks.size();
            Material alphaMaterial = gameStartBlocks.get(alphaIndex);
            Material betaMaterial = gameStartBlocks.get(betaIndex);
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
                    if (!player.getInventory().contains(createFlag())) {
                        player.getInventory().addItem(createFlag());
                    }
                });
            }
        } else if (fieldBlocks.contains(block)) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            if (meta.hasCustomModelDataComponent()) {
                //noinspection UnstableApiUsage
                CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
                //noinspection UnstableApiUsage
                if (cmd.getStrings().contains("mine")) {
                    event.setCancelled(true);
                    defer(() -> {
                        Block replacedBlock = event.getBlock();
                        replacedBlock.setType(Material.BARRIER);
                        //noinspection OptionalGetWithoutIsPresent
                        GameField field = GameField.fromMaterial(block).get();
                        field.spawnItemDisplay(replacedBlock);
                    });
                }
            }
        }
    }

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
                if (result != null) {
                    Block block = result.getHitBlock();
                    if (block != null) {
                        if (block.getType() == GameField.UNKNOWN.block) {
                            defer(() -> {
                                if (game.toggleFlag(block.getX(), block.getZ())) {
                                    games.remove(game);
                                } else {
                                    player.getInventory().addItem(createFlag());
                                    event.getItemDrop().remove();
                                }
                            });
                        } else if (block.getType() == GameField.NONE.block) {
                            Collection<ItemDisplay> displays = block.getRelative(BlockFace.UP)
                                .getLocation().toCenterLocation()
                                .getNearbyEntitiesByType(ItemDisplay.class, .1);
                            if (!displays.isEmpty()) {
                                ItemDisplay first = displays.stream().findFirst().get();
                                if (first.getItemStack().getType() == GameField.FLAG.block) {
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
                }
            }
        }
    }

    @EventHandler
    public void equipGlasses(InventoryClickEvent event) {
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
