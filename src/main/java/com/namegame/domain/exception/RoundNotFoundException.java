package com.namegame.domain.exception;

import java.util.UUID;

public class RoundNotFoundException extends RuntimeException {

    public RoundNotFoundException(UUID gameId, int roundNumber) {
        super("Round " + roundNumber + " not found for game: " + gameId);
    }
}
