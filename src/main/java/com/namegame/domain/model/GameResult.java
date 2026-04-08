package com.namegame.domain.model;

import java.util.List;
import java.util.UUID;

public record GameResult(
        UUID gameId,
        int totalRounds,
        int correctAnswers,
        int incorrectAnswers,
        double correctPercentage,
        double incorrectPercentage,
        List<RoundAnswer> roundDetails,
        long totalTimeMillis,
        double averageReactionTimeMillis
) {}
