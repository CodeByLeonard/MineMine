package edu.shch.mine.game.event;

import edu.shch.mine.game.logic.XRayHelmetLogic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static edu.shch.mine.util.Utils.defer;

public class InventoryClickHandler implements Listener {
    private static InventoryClickHandler instance;
    private InventoryClickHandler() {}

    @EventHandler
    public void equipGlasses(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player player) {
            defer(() -> XRayHelmetLogic.getInstance().checkHelmet(player));
        }
    }

    public static InventoryClickHandler getInstance() {
        if (instance == null) {
            instance = new InventoryClickHandler();
        }
        return instance;
    }
}
