package edu.shch.mine;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("unused")
public class MinePlugin extends JavaPlugin implements Listener {
    public static MinePlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        Bukkit.getPluginManager().registerEvents(
                GameListener.getInstance(),
                this
        );
    }
}