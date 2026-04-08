package com.namegame.domain.exception;

public class RoundAlreadyAnsweredException extends RuntimeException {

    public RoundAlreadyAnsweredException(int roundNumber) {
        super("Round " + roundNumber + " has already been answered");
    }
}
