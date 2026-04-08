package com.namegame.application.dto;

import java.time.Instant;
import java.util.UUID;

public record SubmitAnswerCommand(
        UUID gameId,
        int roundNumber,
        UUID selectedPersonId,
        Instant clientTimestamp
) {}
