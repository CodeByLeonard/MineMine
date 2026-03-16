package edu.shch.mine;

import edu.shch.mine.game.GameListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class MineSweeperPlugin extends JavaPlugin implements Listener {
    public static MineSweeperPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(
            GameListener.getInstance(),
            this
        );
    }
}