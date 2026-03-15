package com.ranoe.mineMine.listeners;

import com.ranoe.mineMine.MineGame;
import com.ranoe.mineMine.commands.SetupCommand;
import com.ranoe.mineMine.util.MineUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PlayerInteractListener implements Listener {
    public static PlayerInteractListener instance;
    private PlayerInteractListener() {}

    public static PlayerInteractListener getInstance() {
        if (instance == null) {
            instance = new PlayerInteractListener();
        }
        return instance;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Block block = event.getClickedBlock();

        if (event.getAction().isLeftClick() && item.getType() == Material.STICK) { event.setCancelled(true); }

        if (item.getType() != Material.STICK || block == null) return;

        // Check if correct Stick
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasEnchantmentGlintOverride() ||
            !meta.getEnchantmentGlintOverride()
        ) return;

        if (block.getType() == Material.SMOOTH_STONE) {
            if (event.getAction().isLeftClick()) {
                MineGame game = SetupCommand.game;
                if (game != null) {
                    game.reveal(block);
                }
            } else if (event.getAction().isRightClick()) {
                MineGame game = SetupCommand.game;
                if (game != null) {
                    game.flag(block);
                }
            }
        } else if (block.getType() == Material.LODESTONE) {
            if (event.getAction().isLeftClick()) {
                player.sendRichMessage(MineUtils.getPrefix() + "You have placed a flag there!");
            } else if (event.getAction().isRightClick()) {
                MineGame game = SetupCommand.game;
                if (game != null) {
                    game.unflag(block);
                }
            }
        }
    }
}
