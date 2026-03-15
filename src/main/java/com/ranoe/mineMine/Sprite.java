package com.ranoe.mineMine;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import static org.bukkit.Material.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public enum Sprite {
    ZERO("74c284a4e974005ea8d1d4d0674ec0894efd8f6dd0248639a6cfa94f85388", STONE),
    ONE("67fac71e36d50a1ad2e2c10e32e0c59eedefb0d35446a8fb4848138fe26fc9", LIGHT_BLUE_CONCRETE),
    TWO("b4968955bd948708041e12b164adfcdb46399c2d381059ed71ce74ae24f8df", GREEN_CONCRETE),
    THREE("1f678cfcc4eeb259d8e57b26f4f6a35a76d7437043be6c23ae855c47c8a2e9", RED_CONCRETE),
    FOUR("bb6971abb18bca9fafaad9ed5a1596132beccffb88a31c829320c867ee477", BLUE_CONCRETE),
    FIVE("c6206e47614c8f983413dee332f2f32e8da37fa57c4ceba1d14b1643b25957", STRIPPED_MANGROVE_WOOD),
    SIX("66f97e563d85dc4d73871d4cdfcc26d8cd44e89fafb1504c8d9a2ac5a56c", CYAN_CONCRETE),
    SEVEN("87ef6185add419735793c8c2a847d9c4e391a2c5b9b2ec262cea95575b0d0", GRAY_CONCRETE),
    EIGHT("8271cdd38e8a7c74231af8a155618f4ffcb7f917e8826c2b3c1836d1bd116d3", LIGHT_GRAY_CONCRETE),
    FLAG("784a7fcb247406e353a36e556ad19578c3ebe4e15581db106d15a5cb9dad", LODESTONE),
    UNKNOWN("c7f1ea26ea5e685b2f2f8764901be914fe35559cb1ecb1ef34b7e46abc8ee540", SMOOTH_STONE),
    BOMB("9b20ff173bd17b2c4f2eb21f3c4b43841a14b31dfbfd354a3bec8263af562b", TNT);

    final URL url;
    final Material material;
    ItemStack head;
    Sprite(String textureHash, Material material) {
        try {
            //noinspection HttpUrlsUsage
            this.url = new URI("http://textures.minecraft.net/texture/%s".formatted(textureHash)).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
        this.material = material;
        System.out.printf("%s has been loaded at %s.%n", this.name(), System.currentTimeMillis());
    }

    public ItemStack getHead() {
        if (this.head == null) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            PlayerProfile profile = Bukkit.getServer().createProfile("spriteProfile");
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(this.url);
            profile.setTextures(textures);
            meta.setPlayerProfile(profile);
            TextColor color = TextColor.color(150, 74, 149);
            meta.displayName(Component.text("Tile " + this.ordinal(), color, TextDecoration.ITALIC));
            head.setItemMeta(meta);
            this.head = head;
        }
        return this.head;
    }

    public void spawn(Location location) {
        ArmorStand armorStand = (ArmorStand) location.getWorld().spawnEntity(
                location.toCenterLocation().subtract(0, 1.3, 0), EntityType.ARMOR_STAND
        );
        armorStand.setVisible(false);
        armorStand.setMarker(true);
        armorStand.setGravity(false);
        armorStand.setBasePlate(false);
        armorStand.setArms(false);
        armorStand.getEquipment().setHelmet(getHead());
    }

    public Material getMaterial() {
        return material;
    }
}
