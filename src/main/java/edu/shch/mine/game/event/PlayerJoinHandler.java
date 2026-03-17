package edu.shch.mine.game.event;

import edu.shch.mine.MineSweeperPlugin;
import edu.shch.mine.game.logic.XRayHelmetLogic;
import edu.shch.mine.util.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static edu.shch.mine.util.Utils.defer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerJoinHandler implements Listener {
    @Getter(lazy = true)
    private static final PlayerJoinHandler instance = new PlayerJoinHandler();

    @EventHandler
    public void checkGlasses(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setupTextures(player);
        defer(() -> XRayHelmetLogic.checkHelmet(player));
    }

    private static void setupTextures(Player player) {
        MineSweeperPlugin plugin = MineSweeperPlugin.instance;
        try {
            int port = plugin.random.nextInt(1 << 10, 1 << 16);
            Utils.serveResources(port, player);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            plugin.getLogger()
                .severe("Couldn't send Texture Pack to %s".formatted(player.getName()));
        }
    }
}
