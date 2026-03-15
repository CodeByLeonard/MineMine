package com.ranoe.mineMine.listeners;

import com.ranoe.mineMine.util.MineLogic;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

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
    public void onPlayerInteract(PlayerInteractEvent event) throws MalformedURLException, URISyntaxException {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Block block = event.getClickedBlock();

        if (event.getAction().isLeftClick() && item.getType() == Material.STICK) { event.setCancelled(true); }
        if (event.getHand() == EquipmentSlot.OFF_HAND) {return;}

        if (item.getType() == Material.STICK && block != null) {
            if (item.getItemMeta().hasEnchantmentGlintOverride() && item.getItemMeta().getEnchantmentGlintOverride()) { // Check if correct Stick
                if (block.getRelative(0, -1, 0).getType() == Material.BARREL) { //Check if on the minefield
                    if (block.getType() == Material.SMOOTH_STONE) {
                        if (event.getAction().isLeftClick()) {
                            MineLogic.revealBlock(block);
                        } else if (event.getAction().isRightClick()) {
                            MineLogic.flagBlock(block);
                        }
                    } else if (block.getType() == Material.LODESTONE) {
                        if (event.getAction().isLeftClick()) {
                            player.sendRichMessage(MineLogic.getPrefix() + "You have placed a flag there!");
                        } else if (event.getAction().isRightClick()) {
                            MineLogic.unflagBlock(block);
                        }
                    }
                }
            }
        }
    }
}
