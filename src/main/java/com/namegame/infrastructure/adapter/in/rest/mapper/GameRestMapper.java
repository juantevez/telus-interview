package com.namegame.infrastructure.adapter.in.rest.mapper;

import com.namegame.application.dto.*;
import com.namegame.domain.model.Game;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class GameRestMapper {

    public StartGameCommand toStartCommand(int totalRounds, int facesPerRound) {
        return new StartGameCommand(totalRounds, facesPerRound);
    }

    public Map<String, Object> toStartGameResponse(Game game) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("gameId", game.getId());
        response.put("totalRounds", game.getTotalRounds());
        response.put("facesPerRound", game.getFacesPerRound());
        response.put("status", game.getStatus().name());
        response.put("currentRound", 1);
        response.put("createdAt", game.getCreatedAt());
        return response;
    }

    public SubmitAnswerCommand toSubmitCommand(UUID gameId, int roundNumber,
                                               UUID selectedPersonId, Instant clientTimestamp) {
        return new SubmitAnswerCommand(gameId, roundNumber, selectedPersonId, clientTimestamp);
    }
}
