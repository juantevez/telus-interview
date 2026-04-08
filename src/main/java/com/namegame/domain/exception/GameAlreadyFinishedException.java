package com.namegame.domain.exception;

import java.util.UUID;

public class GameAlreadyFinishedException extends RuntimeException {

    public GameAlreadyFinishedException(UUID gameId) {
        super("Game is already finished: " + gameId);
    }
}
