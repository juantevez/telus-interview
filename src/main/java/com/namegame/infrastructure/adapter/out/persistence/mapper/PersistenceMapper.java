package com.namegame.infrastructure.adapter.out.persistence.mapper;

import com.namegame.domain.enums.GameStatus;
import com.namegame.domain.enums.RoundStatus;
import com.namegame.domain.model.Game;
import com.namegame.domain.model.Round;
import com.namegame.infrastructure.adapter.out.persistence.entity.GameEntity;
import com.namegame.infrastructure.adapter.out.persistence.entity.RoundEntity;

import java.util.List;

public class PersistenceMapper {

    public static Round toDomain(RoundEntity e) {
        Round r = new Round();
        r.setId(e.getId());
        r.setRoundNumber(e.getRoundNumber());
        r.setTargetPersonId(e.getTargetPersonId());
        r.setOptionPersonIds(e.getOptionPersonIds());
        r.setStatus(RoundStatus.valueOf(e.getStatus()));
        r.setSelectedPersonId(e.getSelectedPersonId());
        r.setCorrect(e.getCorrect());
        r.setPresentedAt(e.getPresentedAt());
        r.setAnsweredAt(e.getAnsweredAt());
        r.setReactionTimeMillis(e.getReactionTimeMillis());
        return r;
    }

    public static RoundEntity toEntity(Round r, GameEntity gameEntity) {
        RoundEntity e = new RoundEntity();
        e.setId(r.getId());
        e.setGame(gameEntity);
        e.setRoundNumber(r.getRoundNumber());
        e.setTargetPersonId(r.getTargetPersonId());
        e.setOptionPersonIds(r.getOptionPersonIds());
        e.setStatus(r.getStatus().name());
        e.setSelectedPersonId(r.getSelectedPersonId());
        e.setCorrect(r.getCorrect());
        e.setPresentedAt(r.getPresentedAt());
        e.setAnsweredAt(r.getAnsweredAt());
        e.setReactionTimeMillis(r.getReactionTimeMillis());
        return e;
    }

    public static Game toDomain(GameEntity e) {
        List<Round> rounds = e.getRounds().stream()
                .map(PersistenceMapper::toDomain)
                .toList();

        Game g = new Game();
        g.setId(e.getId());
        g.setTotalRounds(e.getTotalRounds());
        g.setFacesPerRound(e.getFacesPerRound());
        g.setStatus(GameStatus.valueOf(e.getStatus()));
        g.setRounds(rounds);
        g.setCreatedAt(e.getCreatedAt());
        g.setFinishedAt(e.getFinishedAt());
        g.setVersion(e.getVersion());
        return g;
    }

    public static GameEntity toEntity(Game g) {
        GameEntity e = new GameEntity();
        e.setId(g.getId());
        e.setTotalRounds(g.getTotalRounds());
        e.setFacesPerRound(g.getFacesPerRound());
        e.setStatus(g.getStatus().name());
        e.setCreatedAt(g.getCreatedAt());
        e.setFinishedAt(g.getFinishedAt());

        List<RoundEntity> roundEntities = g.getRounds().stream()
                .map(r -> toEntity(r, e))
                .toList();
        e.setRounds(roundEntities);
        e.setVersion(g.getVersion());
        return e;
    }
}
