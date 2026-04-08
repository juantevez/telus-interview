package com.namegame.domain.port.in;

import com.namegame.domain.model.Round;

import java.time.Instant;
import java.util.UUID;

public interface SubmitAnswerPort {

    Round submitAnswer(UUID gameId, int roundNumber, UUID selectedPersonId, Instant clientTimestamp);
}
