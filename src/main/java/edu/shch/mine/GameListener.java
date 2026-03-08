package edu.shch.mine;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public class GameListener implements Listener {
    private static GameListener instance;
    private GameListener() {}

    public static GameListener getInstance() {
        if (instance == null) {
            instance = new GameListener();
        }
        return instance;
    }

    @EventHandler
    public void initiateGame(BlockPlaceEvent event) {
        List<Material> blocks = List.of(
                Material.TNT,
                Material.SMOOTH_STONE,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE
        );

        Material block = event.getBlock().getType();
        if (blocks.contains(block)) {
            int index = blocks.indexOf(block);
            int alphaIndex = (index + 1) % blocks.size();
            int betaIndex = (index + 2) % blocks.size();
            Material alphaMaterial = blocks.get(alphaIndex);
            Material betaMaterial = blocks.get(betaIndex);
            int alphaPos =  alphaIndex - index;
            int betaPos = betaIndex - index;

            Material alphaTestMaterial = event.getBlock().getRelative(0, alphaPos, 0).getType();
            Material betaTestMaterial = event.getBlock().getRelative(0, betaPos, 0).getType();

            if (alphaMaterial == alphaTestMaterial && betaMaterial == betaTestMaterial) {
                event.getPlayer().sendMessage("Start...");
            } else {
                event.getPlayer().sendMessage("Not yet...");
            }
        }
    }
}
