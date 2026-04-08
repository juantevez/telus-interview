package com.namegame.domain.model;

public record RoundAnswer(
        int roundNumber,
        String targetName,
        String selectedName,
        boolean correct,
        long reactionTimeMillis
) {}
