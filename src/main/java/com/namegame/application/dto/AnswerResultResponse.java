package com.namegame.application.dto;

import java.util.UUID;

public record AnswerResultResponse(
        int roundNumber,
        boolean correct,
        UUID targetPersonId,
        String targetName,
        UUID selectedPersonId,
        String selectedName,
        long reactionTimeMillis,
        boolean hasNextRound,
        Integer nextRoundNumber
) {}
