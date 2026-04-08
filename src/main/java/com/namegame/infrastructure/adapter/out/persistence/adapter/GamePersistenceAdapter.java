package com.namegame.infrastructure.adapter.out.persistence.adapter;

import com.namegame.domain.model.Game;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.infrastructure.adapter.out.persistence.entity.GameEntity;
import com.namegame.infrastructure.adapter.out.persistence.mapper.PersistenceMapper;
import com.namegame.infrastructure.adapter.out.persistence.repository.GameJpaRepository;

import java.util.Optional;
import java.util.UUID;

public class GamePersistenceAdapter implements GameRepositoryPort {

    private final GameJpaRepository jpaRepository;

    public GamePersistenceAdapter(GameJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Game save(Game game) {
        GameEntity entity = PersistenceMapper.toEntity(game);
        GameEntity saved = jpaRepository.save(entity);
        return PersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Game> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(PersistenceMapper::toDomain);
    }
}
