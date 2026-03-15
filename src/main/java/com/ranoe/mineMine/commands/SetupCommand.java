package com.ranoe.mineMine.commands;

import com.ranoe.mineMine.util.MineLogic;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Objects;

@NullMarked
public class SetupCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length == 0) {
            source.getSender().sendRichMessage(MineLogic.getPrefix() + "Try typing in /minesweeper setup!");
        } else if (args[0].equalsIgnoreCase("setup")) {
            if (source.getExecutor() instanceof Player) {
                source.getSender().sendRichMessage(MineLogic.getPrefix() + "The game has been set up, enjoy :)");
                try {
                    MineLogic.setupTiles(Objects.requireNonNull(((Player) source.getExecutor()).getPlayer()));
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            } else {
                source.getSender().sendRichMessage(MineLogic.getPrefix() + "You must be a player to execute this command!");
            }
        } else if (args[0].equalsIgnoreCase("sprite")) {
            if (source.getExecutor() instanceof Player) {
                if (args.length != 2) {
                    source.getExecutor().sendRichMessage(MineLogic.getPrefix() + "Provide the desired sprite number!");
                    return;
                }
                int spriteID;

                try {
                    spriteID = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    source.getExecutor().sendRichMessage(MineLogic.getPrefix() + "Sprite ID must be a number!");
                    return;
                }
                source.getSender().sendRichMessage(MineLogic.getPrefix() + "A test-sprite has been spawned!");

                try {
                    if (spriteID >= MineLogic.spriteList().size()) {
                        source.getSender().sendRichMessage(MineLogic.getPrefix() + "Sprite ID invalid!");
                        return;
                    }
                    MineLogic.spawnSprite(source.getExecutor().getLocation(), spriteID);
                } catch (URISyntaxException | MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            } else {
                source.getSender().sendRichMessage(MineLogic.getPrefix() + "You must be a player to execute this command!");
            }
        } else if (args[0].equalsIgnoreCase("sprites")) {
            if (source.getExecutor() instanceof Player player) {
                source.getSender().sendRichMessage(MineLogic.getPrefix() + "You have received all sprites!");
                int slotCounter = 0;
                for (int i = 1; i <= 8; i++) {
                    try {
                        player.getInventory().setItem(slotCounter, MineLogic.spawnSpriteHead(i));
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                    slotCounter += 1;
                    if (slotCounter == 4) {slotCounter+=1;}
                }
            } else {
                source.getSender().sendRichMessage(MineLogic.getPrefix() + "You must be a player to execute this command!");
            }
        }
    }

    @Override
    public @Nullable String permission() {
        return "minesweeper";
    }
}
