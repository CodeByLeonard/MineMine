package edu.shch.mine;

public class Utils {
    public static void defer(Runnable action) {
        MinePlugin.instance.getServer().getScheduler().runTaskLater(
            MinePlugin.instance,
            action,
            1
        );
    }
}
