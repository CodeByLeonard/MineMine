package edu.shch.mine.game.logic;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.List;

public class FlagLogic {
    @SuppressWarnings("UnstableApiUsage")
    public static @NonNull ItemStack createFlag() {
        ItemStack flag = ItemStack.of(Material.RED_BANNER);
        ItemMeta itemMeta = flag.getItemMeta();
        CustomModelDataComponent cmd = itemMeta.getCustomModelDataComponent();
        cmd.setStrings(List.of("flag"));
        itemMeta.setCustomModelDataComponent(cmd);
        flag.setItemMeta(itemMeta);
        return flag.asOne();
    }

    public static void unflag(GameState game, Block block, Runnable cancel) {
        Collection<ItemDisplay> displays = block.getRelative(BlockFace.UP)
            .getLocation().toCenterLocation()
            .getNearbyEntitiesByType(ItemDisplay.class, .1);

        boolean handled = false;
        for (ItemDisplay display : displays) {
            if (!handled && display.getItemStack().equals(createFlag())) {
                toggleFlag(game, block, cancel);
                handled = true;
            } else {
                //noinspection UnstableApiUsage
                MineSweeperPlugin.instance.getLogger()
                    .warning("Ignoring Strange Item Display during Unflagging: " + display.getAsString());
            }
        }
    }

    public static void toggleFlag(GameState game, Block block, Runnable cancel) {
        game.toggleFlag(block.getX(), block.getZ());
        if (game.isNotFinished()) {
            cancel.run();
        }
    }

    public static void giveFlag(PlayerInventory inventory) {
        ItemStack flag = createFlag();
        if (!inventory.contains(flag)) {
            inventory.addItem(flag);
        }
    }
}
