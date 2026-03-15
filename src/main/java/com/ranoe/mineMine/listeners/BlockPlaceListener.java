package com.ranoe.mineMine.listeners;

import com.ranoe.mineMine.util.MineLogic;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;

public class BlockPlaceListener implements Listener {
    public static BlockPlaceListener instance;
    private BlockPlaceListener () {}

    public static BlockPlaceListener getInstance() {
        if (instance == null) {
            instance = new BlockPlaceListener();
        }
        return instance;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Material blockMaterial = event.getBlock().getType();
        List<Material> summoningBlocks = List.of(
                Material.TNT,
                Material.SMOOTH_STONE,
                Material.HEAVY_WEIGHTED_PRESSURE_PLATE
        );

        if (summoningBlocks.contains(blockMaterial)) {
            int index = summoningBlocks.indexOf(blockMaterial);

            int alphaIndex = (index + 1) % summoningBlocks.size();
            int betaIndex = (index + 2) % summoningBlocks.size();

            int alphaPos = alphaIndex - index;
            int betaPos = betaIndex - index;

            Material alphaMaterial = summoningBlocks.get(alphaIndex);
            Material betaMaterial = summoningBlocks.get(betaIndex);

            Material alphaTest = event.getBlock().getRelative(0, alphaPos, 0).getType();
            Material betaTest = event.getBlock().getRelative(0, betaPos, 0).getType();

            if (alphaMaterial == alphaTest && betaMaterial == betaTest) {
                //MineLogic.generateMine(event, event.getBlock().getY() - index + 2);
            } // else { event.getPlayer().sendMessage("Almost there.."); }
        }
    }
}
