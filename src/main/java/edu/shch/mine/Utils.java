package edu.shch.mine;

import org.joml.Vector2i;

import java.util.Comparator;

public class Utils {
    public static void defer(Runnable action) {
        MinePlugin.instance.getServer().getScheduler().runTaskLater(
            MinePlugin.instance,
            action,
            1
        );
    }

    static class Vec2iComparator implements Comparator<Vector2i> {
        @Override
        public int compare(Vector2i a, Vector2i b) {
            int initial = Integer.compare(a.x, b.x);
            return initial == 0 ? Integer.compare(a.y, b.y) : initial;
        }
    }
}
