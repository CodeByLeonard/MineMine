package edu.shch.mine.game.event;

import edu.shch.mine.game.logic.XRayHelmetLogic;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import static edu.shch.mine.util.Utils.defer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryClickHandler implements Listener {
    @Getter(lazy = true)
    private static final InventoryClickHandler instance = new InventoryClickHandler();

    @EventHandler
    public void equipGlasses(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Player player) {
            defer(() -> XRayHelmetLogic.checkHelmet(player));
        }
    }
}
