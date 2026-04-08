package com.namegame.domain.exception;

public class InsufficientPeopleException extends RuntimeException {

    public InsufficientPeopleException(long available, int required) {
        super("Not enough active persons to start a game. Available: " + available + ", required: " + required);
    }
}
