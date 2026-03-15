package com.ranoe.mineMine;

import com.ranoe.mineMine.commands.SetupCommand;
import com.ranoe.mineMine.listeners.PlayerInteractListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineMine extends JavaPlugin {

    @Override
    public void onEnable() {
        registerCommand("minesweeper", new SetupCommand());

        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(PlayerInteractListener.getInstance(), this);
        //pluginManager.registerEvents(BlockPlaceListener.getInstance(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
