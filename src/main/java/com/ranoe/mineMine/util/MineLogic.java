package com.ranoe.mineMine.util;

import com.ranoe.mineMine.Sprite;
import io.papermc.paper.math.Rotations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

public class MineLogic {
    public static String getPrefix() {
        return "<red><bold>MINESWEEPER</red> <dark_gray>»</dark_gray> ";
    }

    public static void setupTiles(Player player) throws MalformedURLException, URISyntaxException {
        killStandsInChunk(player.getChunk());
        Block block = player.getLocation().getBlock();
        Chunk chunk = block.getChunk();
        int playerY = block.getY();
        int maxY = block.getWorld().getMaxHeight() -1;
        int endY = Math.min(playerY + 10, maxY);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = playerY; y <= endY; y++) {
                    Block currentBlock = chunk.getBlock(x ,y, z);
                    currentBlock.setType(Material.AIR, false);
                }
                chunk.getBlock(x, playerY - 1 ,z).setType(Material.SMOOTH_STONE);
                chunk.getBlock(x, playerY - 2 ,z).setType(Material.BARREL);
            }
        }
        player.getInventory().setItem(4, getMagicWand());
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

    private static final Random random = new Random();
    public static void revealBlock(Block block) {
        int randomInt = random.nextInt(Sprite.values().length);
        Sprite randomSprite = Sprite.values()[randomInt];
        block.setType(randomSprite.getMaterial(), false);
        Location spawnLocation = block.getLocation();
        killStandOnBlock(spawnLocation);
        randomSprite.spawn(spawnLocation);
        if (randomSprite == Sprite.BOMB) {
            block.getWorld().spawnParticle(Particle.EXPLOSION, spawnLocation, 2);
            block.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, spawnLocation, 1);
            block.getWorld().spawnParticle(Particle.LAVA, spawnLocation, 1);
            block.getWorld().playSound(spawnLocation, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
        }
    }

    public static void flagBlock(Block block) {
        Sprite.FLAG.spawn(block.getLocation());
        block.setType(Sprite.FLAG.getMaterial());
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
