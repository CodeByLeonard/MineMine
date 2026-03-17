package edu.shch.mine.util;

public record Pair<T, U>(T x, U y) {
    public static <T, U> Pair<T, U> of(T x, U y) {
        return new Pair<>(x, y);
    }
}
