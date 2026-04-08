package com.namegame.domain.port.in;

import com.namegame.domain.model.Round;

import java.util.UUID;

public interface GetRoundPort {

    Round getRound(UUID gameId, int roundNumber);
}
