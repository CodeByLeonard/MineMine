package edu.shch.mine.util;

import org.joml.Vector2i;

import java.util.Comparator;

public class Vec2iComparator implements Comparator<Vector2i> {
    @Override
    public int compare(Vector2i a, Vector2i b) {
        int initial = Integer.compare(a.x, b.x);
        return initial == 0 ? Integer.compare(a.y, b.y) : initial;
    }
}