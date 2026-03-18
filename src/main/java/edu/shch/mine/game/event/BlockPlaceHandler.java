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
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

import java.util.List;

import static edu.shch.mine.util.Utils.defer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlockPlaceHandler implements Listener {
    @Getter(lazy = true)
    private static final BlockPlaceHandler instance = new BlockPlaceHandler();

    private static final List<Material> TRASH = List.of(
        // Seeds
        Material.WHEAT_SEEDS,
        // Tree Drops
        Material.SPRUCE_SAPLING,
        // Flowers, Mushrooms
        Material.POPPY, Material.PEONY
    );

    private static final List<Material> gameStartBlocks = List.of(
        Material.TNT,
        Material.SMOOTH_STONE,
        Material.HEAVY_WEIGHTED_PRESSURE_PLATE
    );

    @EventHandler
    public void initiateGame(BlockPlaceEvent event) {
        Material material = event.getBlock().getType();
        if (gameStartBlocks.contains(material)) {
            checkGameStart(event.getPlayer(), event.getBlock());
        } else {
            Runnable cancel = () -> event.setCancelled(true);

            if (GameField.NON_MINE_MATERIALS.contains(material)) {
                //noinspection OptionalGetWithoutIsPresent
                placeFieldBlock(GameField.fromMaterial(material).get(), event.getItemInHand(), event.getBlock(), cancel);
            } else if (material == Material.RED_BANNER) {
                placeFlagBlock(event.getBlock(), cancel);
            }
        }
    }

    private static void placeFlagBlock(Block block, Runnable cancel) {
        Block below = block.getRelative(BlockFace.DOWN);
        for (GameState game : MineSweeperPlugin.instance.games) {
            if (ChunkUtils.sameChunk(game.locator, below)) {
                Material type = below.getType();
                if (type == GameField.NONE.type || type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                    FlagLogic.toggleFlag(game, block, cancel);
                } else {
                    cancel.run();
                }
            }
        }
    }

    private static void placeFieldBlock(GameField field, ItemStack item, Block block, Runnable cancel) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasCustomModelDataComponent()) {
            //noinspection UnstableApiUsage
            CustomModelDataComponent cmd = meta.getCustomModelDataComponent();
            //noinspection UnstableApiUsage
            if (cmd.getStrings().contains("mine")) {
                cancel.run();
                defer(() -> {
                    block.setType(Material.BARRIER);
                    field.spawnItemDisplay(block.getLocation());
                });
            }
        }
    }

    private static void checkGameStart(Player player, Block block) {
        int index = gameStartBlocks.indexOf(block.getType());
        int alphaIndex = (index + 1) % gameStartBlocks.size();
        int betaIndex = (index + 2) % gameStartBlocks.size();
        Material alphaMaterial = gameStartBlocks.get(alphaIndex);
        Material betaMaterial = gameStartBlocks.get(betaIndex);
        int alphaPos = alphaIndex - index;
        int betaPos = betaIndex - index;

        Material alphaTestMaterial = block.getRelative(0, alphaPos, 0).getType();
        Material betaTestMaterial = block.getRelative(0, betaPos, 0).getType();

        if (alphaMaterial == alphaTestMaterial && betaMaterial == betaTestMaterial) {
            startGame(block.getRelative(0, -index, 0), player);
        }
    }

    private static void startGame(Block locator, Player player) {
        MineSweeperPlugin plugin = MineSweeperPlugin.instance;
        plugin.games.add(GameState.from(player, locator,
            (state) -> defer(() -> plugin.games.remove(state))));

        Server server = player.getServer();
        defer(() -> {
            for (Entity e : server.selectEntities(server.getConsoleSender(), "@e[type=item]")) {
                if (e instanceof Item item && TRASH.contains(item.getItemStack().getType())) {
                    item.remove();
                }
            }
            FlagLogic.giveFlag(player.getInventory());
        });
    }
}
