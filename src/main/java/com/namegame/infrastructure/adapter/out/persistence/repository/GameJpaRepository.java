package com.namegame.infrastructure.adapter.out.persistence.repository;

import com.namegame.infrastructure.adapter.out.persistence.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameJpaRepository extends JpaRepository<GameEntity, UUID> {}
