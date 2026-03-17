package edu.shch.mine;

import edu.shch.mine.game.GameState;
import edu.shch.mine.game.event.*;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("unused")
public class MineSweeperPlugin extends JavaPlugin implements Listener {
    public Random random = new Random(42L);
    public final ArrayList<GameState> games = new ArrayList<>();

    public static MineSweeperPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        List<Listener> listeners = List.of(
            BlockBreakHandler.getInstance(),
            BlockPlaceHandler.getInstance(),
            InventoryClickHandler.getInstance(),
            PlayerDropItemHandler.getInstance(),
            PlayerInteractHandler.getInstance(),
            PlayerJoinHandler.getInstance()
        );

        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}