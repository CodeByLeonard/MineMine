package com.ranoe.mineMine.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.math.Rotations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MineLogic {
    public static String getPrefix() {
        return "<red><bold>MINESWEEPER</red> <dark_gray>»</dark_gray> ";
    }

    public static List<String> spriteList() {
        return List.of(
                "http://textures.minecraft.net/texture/74c284a4e974005ea8d1d4d0674ec0894efd8f6dd0248639a6cfa94f85388", //0 Tile
                "http://textures.minecraft.net/texture/67fac71e36d50a1ad2e2c10e32e0c59eedefb0d35446a8fb4848138fe26fc9", //1 Tile
                "http://textures.minecraft.net/texture/b4968955bd948708041e12b164adfcdb46399c2d381059ed71ce74ae24f8df", //2 Tile
                "http://textures.minecraft.net/texture/1f678cfcc4eeb259d8e57b26f4f6a35a76d7437043be6c23ae855c47c8a2e9", //3 Tile
                "http://textures.minecraft.net/texture/bb6971abb18bca9fafaad9ed5a1596132beccffb88a31c829320c867ee477", //4 Tile
                "http://textures.minecraft.net/texture/c6206e47614c8f983413dee332f2f32e8da37fa57c4ceba1d14b1643b25957", //5 Tile
                "http://textures.minecraft.net/texture/66f97e563d85dc4d73871d4cdfcc26d8cd44e89fafb1504c8d9a2ac5a56c", //6 Tile
                "http://textures.minecraft.net/texture/87ef6185add419735793c8c2a847d9c4e391a2c5b9b2ec262cea95575b0d0", //7 Tile
                "http://textures.minecraft.net/texture/8271cdd38e8a7c74231af8a155618f4ffcb7f917e8826c2b3c1836d1bd116d3", //8 Tile
                "http://textures.minecraft.net/texture/784a7fcb247406e353a36e556ad19578c3ebe4e15581db106d15a5cb9dad", //Flag Tile
                "http://textures.minecraft.net/texture/c7f1ea26ea5e685b2f2f8764901be914fe35559cb1ecb1ef34b7e46abc8ee540", //Unknown Tile - Default Block
                "http://textures.minecraft.net/texture/9b20ff173bd17b2c4f2eb21f3c4b43841a14b31dfbfd354a3bec8263af562b" //Bomb Tile
        );
    }

    public static List<Material> blockList() {
        return List.of(
                Material.STONE, //0 Tile
                Material.LIGHT_BLUE_CONCRETE, //1 Tile
                Material.GREEN_CONCRETE, //2 Tile
                Material.RED_CONCRETE, //3 Tile
                Material.BLUE_CONCRETE, //4 Tile
                Material.STRIPPED_MANGROVE_WOOD, //5 Tile
                Material.CYAN_CONCRETE, //6 Tile
                Material.GRAY_CONCRETE, //7 Tile
                Material.LIGHT_GRAY_CONCRETE, //8 Tile
                Material.LODESTONE, //Flag Tile
                Material.SMOOTH_STONE, //10 Tile - Default Block
                Material.TNT //Bomb Tile
        );
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

                spawnSprite(chunk.getBlock(x, playerY - 1, z).getLocation(), 10);
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
    public static void revealBlock(Block block) throws MalformedURLException, URISyntaxException {
        int randomInt = random.nextInt(blockList().size());
        Material randomMaterial = blockList().get(randomInt);
        block.setType(randomMaterial, false);
        killStandOnBlock(block.getLocation());
        spawnSprite(block.getLocation(), randomInt);
        if (randomMaterial == Material.TNT) {
            block.getWorld().spawnParticle(Particle.EXPLOSION, block.getLocation(), 2);
            block.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, block.getLocation(), 1);
            block.getWorld().spawnParticle(Particle.LAVA, block.getLocation(), 1);
            block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1f, 1.5f);
        }
    }

    public static void flagBlock(Block block) throws MalformedURLException, URISyntaxException {
        spawnSprite(block.getLocation(), 9);
        block.setType(Material.LODESTONE);
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

    public static void spawnSprite(Location location, int spriteID) throws URISyntaxException, MalformedURLException {
        Location modifiedLocation = new Location(location.getWorld(), location.x() + 0.5, location.y() - 0.8, location.z() + 0.5);
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(modifiedLocation, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);

        armorStand.getEquipment().setHelmet(spawnSpriteHead(spriteID));
    }

    public static ItemStack spawnSpriteHead(int spriteID) throws URISyntaxException, MalformedURLException {
        URL spriteURL = new URI(spriteList().get(spriteID)).toURL();

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        PlayerProfile profile = Bukkit.getServer().createProfile("spriteProfile");
        PlayerTextures textures = profile.getTextures();
        textures.setSkin(spriteURL);
        profile.setTextures(textures);
        meta.setPlayerProfile(profile);

        meta.displayName(Component.text("Tile " + spriteID, TextColor.color(150, 74, 149), TextDecoration.ITALIC));

        head.setItemMeta(meta);

        return head;
    }
}
