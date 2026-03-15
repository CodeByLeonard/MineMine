package com.ranoe.mineMine.util;

import io.papermc.paper.math.Rotations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class MineUtils {
    public static String getPrefix() {
        return "<red><bold>MINESWEEPER</red> <dark_gray>»</dark_gray> ";
    }

    public static ItemStack getMagicWand() {
        ItemStack wand = new ItemStack(Material.STICK);

        ItemMeta meta = wand.getItemMeta();
        Component name = Component.text("* Magic Wand *").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD);
        meta.displayName(name);

        List<Component> lore = List.of(
                Component.text("Right Click to flag,").color(NamedTextColor.GRAY),
                Component.text("Left Click to try a slot.").color(NamedTextColor.GRAY)
        );
        meta.lore(lore);
        meta.setUnbreakable(true);
        meta.setEnchantmentGlintOverride(true);

        wand.setItemMeta(meta);
        return wand;
    }

    public static void spawnExplosion(Block block) {
        Location spawnLocation = block.getLocation();
        block.getWorld().spawnParticle(Particle.EXPLOSION, spawnLocation, 2);
        block.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, spawnLocation, 1);
        block.getWorld().spawnParticle(Particle.LAVA, spawnLocation, 1);
        block.getWorld().playSound(spawnLocation, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
    }

    public static void unflagBlock(Block block) {
        block.setType(Material.SMOOTH_STONE);
        killStandOnBlock(block.getLocation());
    }

    public static void killStandsInChunk(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ArmorStand) {
                entity.remove();
            }
        }
    }

    public static void killStandOnBlock(Location location) {
        Chunk chunk = location.getChunk();
        int blockX = location.getBlockX();
        int blockZ = location.getBlockZ();

        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof ArmorStand) {
                Location entityLocation = entity.getLocation();
                if (entityLocation.getBlockX() == blockX && entityLocation.getBlockZ() == blockZ) {
                    entity.remove();
                }
            }
        }
    }

    @Deprecated
    public static void spawnFlag(Location location) {
        ArmorStand poleStand = (ArmorStand) location.getWorld().spawnEntity(new Location(location.getWorld(), location.x() + 0.5, location.y() - 0.502, location.z() + 0.5), EntityType.ARMOR_STAND);
        poleStand.setVisible(true);
        poleStand.setMarker(true);
        poleStand.setGravity(false);
        poleStand.setBasePlate(false);
        poleStand.setArms(false);
        poleStand.setRotation(90, 0);
        poleStand.setHeadRotations(Rotations.ofDegrees(-35, 0, 0));

        ArmorStand carpetStand = (ArmorStand) location.getWorld().spawnEntity(new Location(location.getWorld(), location.x() + 0.5 + 0.512 + 0.2, location.y() + 0.08 - 0.21, location.z() + 0.5 + 0.585), EntityType.ARMOR_STAND);
        carpetStand.setVisible(false);
        carpetStand.setMarker(true);
        carpetStand.setGravity(false);
        carpetStand.setBasePlate(false);
        carpetStand.setArms(false);
        carpetStand.getEquipment().setItemInMainHand(new ItemStack(Material.RED_CARPET));
        carpetStand.setRightArmRotations(Rotations.ofDegrees(75 ,0 ,80));
    }
}
