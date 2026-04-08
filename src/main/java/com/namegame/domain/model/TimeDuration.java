package com.namegame.domain.model;

public record TimeDuration(long millis) {

    public static TimeDuration ofMillis(long millis) {
        return new TimeDuration(millis);
    }

    public double toSeconds() {
        return millis / 1000.0;
    }
}
