package com.namegame.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameResultResponse(
        UUID gameId,
        int totalRounds,
        int correctAnswers,
        int incorrectAnswers,
        double correctPercentage,
        double incorrectPercentage,
        long totalTimeMillis,
        double averageReactionTimeMillis,
        List<RoundSummaryDto> rounds,
        Instant finishedAt
) {
    public record RoundSummaryDto(
            int roundNumber,
            String targetName,
            String selectedName,
            boolean correct,
            long reactionTimeMillis
    ) {}
}
