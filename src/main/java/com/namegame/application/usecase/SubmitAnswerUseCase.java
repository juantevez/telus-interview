package com.namegame.application.usecase;

import com.namegame.application.dto.AnswerResultResponse;
import com.namegame.domain.exception.GameNotFoundException;
import com.namegame.domain.model.Game;
import com.namegame.domain.model.Person;
import com.namegame.domain.model.Round;
import com.namegame.domain.port.in.SubmitAnswerPort;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SubmitAnswerUseCase implements SubmitAnswerPort {

    private final GameRepositoryPort gameRepository;
    private final PersonRepositoryPort personRepository;

    public SubmitAnswerUseCase(GameRepositoryPort gameRepository, PersonRepositoryPort personRepository) {
        this.gameRepository = gameRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Round submitAnswer(UUID gameId, int roundNumber, UUID selectedPersonId, Instant clientTimestamp) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        Round round = game.getRoundByNumber(roundNumber);
        round.submitAnswer(selectedPersonId, Instant.now());

        if (game.isLastRound(roundNumber)) {
            game.finish();
        }

        gameRepository.save(game);
        return round;
    }

    public AnswerResultResponse submitAnswerWithDetails(UUID gameId, int roundNumber,
                                                        UUID selectedPersonId, Instant clientTimestamp) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        Round round = game.getRoundByNumber(roundNumber);
        round.submitAnswer(selectedPersonId, Instant.now());

        if (game.isLastRound(roundNumber)) {
            game.finish();
        }

        gameRepository.save(game);

        List<UUID> idsToFetch = List.of(round.getTargetPersonId(), selectedPersonId);

        Map<UUID, Person> persons = personRepository.findAllByIds(idsToFetch)
                .stream()
                .collect(Collectors.toMap(
                        Person::getId,
                        p -> p,
                        (existing, replacement) -> existing)
                );


        Person target   = persons.get(round.getTargetPersonId());
        Person selected = persons.get(selectedPersonId);

        boolean hasNextRound    = !game.isLastRound(roundNumber);
        Integer nextRoundNumber = hasNextRound ? roundNumber + 1 : null;

        return new AnswerResultResponse(
                round.getRoundNumber(),
                Boolean.TRUE.equals(round.getCorrect()),
                round.getTargetPersonId(),
                target   != null ? target.getFullName()   : null,
                selectedPersonId,
                selected != null ? selected.getFullName() : null,
                round.getReactionTimeMillis() == null ? 0 : round.getReactionTimeMillis(),
                hasNextRound,
                nextRoundNumber
        );
    }
}
