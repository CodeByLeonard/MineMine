package edu.shch.mine.game.logic;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameField;
import edu.shch.mine.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

import static edu.shch.mine.util.Utils.defer;

public class FlagLogic {
    private static FlagLogic instance;

    @SuppressWarnings("UnstableApiUsage")
    private @NonNull ItemStack createFlag() {
        ItemStack flag = ItemStack.of(Material.RED_BANNER);
        ItemMeta itemMeta = flag.getItemMeta();
        CustomModelDataComponent cmd = itemMeta.getCustomModelDataComponent();
        cmd.setStrings(List.of("flag"));
        itemMeta.setCustomModelDataComponent(cmd);
        flag.setItemMeta(itemMeta);
        return flag.asOne();
    }

    public void unflag(Item item, GameState game, Block block, PlayerInventory inventory) {
        Collection<ItemDisplay> displays = block.getRelative(BlockFace.UP)
            .getLocation().toCenterLocation()
            .getNearbyEntitiesByType(ItemDisplay.class, .1);

        boolean handled = false;
        for (ItemDisplay display : displays) {
            if (!handled && display.getItemStack().getType() == GameField.FLAG.block) {
                toggleFlag(item, game, block, inventory);
                handled = true;
            } else {
                //noinspection UnstableApiUsage
                MineSweeperPlugin.instance.getLogger()
                    .warning("Ignoring Strange Item Display during Unflagging: " + display.getAsString());
            }
        }
    }

    public void toggleFlag(Item item, GameState game, Block block, PlayerInventory inventory) {
        defer(() -> {
            game.toggleFlag(block.getX(), block.getZ());
            if (game.isNotFinished()) {
                giveFlag(inventory);
                item.remove();
            }
        });
    }

    public void giveFlag(PlayerInventory inventory) {
        ItemStack flag = createFlag();
        if (!inventory.contains(flag)) {
            inventory.addItem(flag);
        }
    }

    public static FlagLogic getInstance() {
        if (instance == null) {
            instance = new FlagLogic();
        }
        return instance;
    }
}
