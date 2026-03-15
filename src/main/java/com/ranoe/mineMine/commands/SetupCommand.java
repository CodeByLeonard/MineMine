package com.ranoe.mineMine.commands;

import com.ranoe.mineMine.MineGame;
import com.ranoe.mineMine.Sprite;
import com.ranoe.mineMine.util.MineUtils;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public class SetupCommand implements BasicCommand {

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length == 0) {
            source.getSender().sendRichMessage(MineUtils.getPrefix() + "Try typing in /minesweeper setup!");
        } else if (args[0].equalsIgnoreCase("setup")) {
            if (source.getExecutor() instanceof Player player) {
                player.sendRichMessage(MineUtils.getPrefix() + "The game has been set up, enjoy :)");
                // MineUtils.setupTiles(Objects.requireNonNull(((Player) source.getExecutor()).getPlayer()));
                new MineGame(player.getLocation()).init(player);
            } else {
                source.getSender().sendRichMessage(MineUtils.getPrefix() + "You must be a player to execute this command!");
            }
        } else if (args[0].equalsIgnoreCase("sprite")) {
            if (source.getExecutor() instanceof Player) {
                if (args.length != 2) {
                    source.getExecutor().sendRichMessage(MineUtils.getPrefix() + "Provide the desired sprite number!");
                    return;
                }
                int spriteID;

                try {
                    spriteID = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    source.getExecutor().sendRichMessage(MineUtils.getPrefix() + "Sprite ID must be a number!");
                    return;
                }
                source.getSender().sendRichMessage(MineUtils.getPrefix() + "A test-sprite has been spawned!");

                if (spriteID >= Sprite.values().length) {
                    source.getSender().sendRichMessage(MineUtils.getPrefix() + "Sprite ID invalid!");
                    return;
                }
                Sprite.values()[spriteID].spawn(source.getExecutor().getLocation());
            } else {
                source.getSender().sendRichMessage(MineUtils.getPrefix() + "You must be a player to execute this command!");
            }
        }
    }

    @Override
    public @Nullable String permission() {
        return "minesweeper";
    }
}
