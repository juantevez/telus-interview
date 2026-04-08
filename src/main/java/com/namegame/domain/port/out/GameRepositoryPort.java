package com.namegame.domain.port.out;

import com.namegame.domain.model.Game;

import java.util.Optional;
import java.util.UUID;

public interface GameRepositoryPort {

    Game save(Game game);

    Optional<Game> findById(UUID id);
}
