package com.namegame.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RoundResponse(
        UUID gameId,
        int roundNumber,
        int totalRounds,
        String targetName,
        List<FaceDto> faces,
        String status,
        Instant presentedAt
) {
    public record FaceDto(UUID personId, String photoUrl) {}
}
