package com.namegame.domain.port.in;

import com.namegame.domain.model.GameResult;

import java.util.UUID;

public interface GetGameResultsPort {

    GameResult getResults(UUID gameId);
}
