package com.namegame.infrastructure.adapter.out.persistence.repository;

import com.namegame.infrastructure.adapter.out.persistence.entity.RoundEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoundJpaRepository extends JpaRepository<RoundEntity, UUID> {}
