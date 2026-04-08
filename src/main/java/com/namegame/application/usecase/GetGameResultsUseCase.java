package com.namegame.application.usecase;

import com.namegame.application.dto.GameResultResponse;
import com.namegame.domain.exception.GameAlreadyFinishedException;
import com.namegame.domain.exception.GameNotFoundException;
import com.namegame.domain.model.Game;
import com.namegame.domain.model.Person;
import com.namegame.domain.model.Round;
import com.namegame.domain.port.in.GetGameResultsPort;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetGameResultsUseCase implements GetGameResultsPort {

    private final GameRepositoryPort gameRepository;
    private final PersonRepositoryPort personRepository;

    public GetGameResultsUseCase(GameRepositoryPort gameRepository, PersonRepositoryPort personRepository) {
        this.gameRepository = gameRepository;
        this.personRepository = personRepository;
    }

    @Override
    public com.namegame.domain.model.GameResult getResults(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        validateFinished(game);
        return game.calculateResult();
    }

    public GameResultResponse getResultResponse(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        validateFinished(game);

        List<Round> rounds = game.getRounds();

        List<UUID> allPersonIds = rounds.stream()
                .flatMap(r -> Stream.concat(
                        Stream.of(r.getTargetPersonId()),
                        r.getSelectedPersonId() != null ? Stream.of(r.getSelectedPersonId()) : Stream.empty()
                ))
                .distinct()
                .toList();

        Map<UUID, Person> persons = personRepository.findAllByIds(allPersonIds)
                .stream().collect(Collectors.toMap(Person::getId, Function.identity()));

        int correct = (int) rounds.stream().filter(r -> Boolean.TRUE.equals(r.getCorrect())).count();
        int incorrect = rounds.size() - correct;
        long totalTime = rounds.stream()
                .mapToLong(r -> r.getReactionTimeMillis() == null ? 0 : r.getReactionTimeMillis())
                .sum();
        double avgReaction = rounds.stream()
                .mapToLong(r -> r.getReactionTimeMillis() == null ? 0 : r.getReactionTimeMillis())
                .average()
                .orElse(0);

        List<GameResultResponse.RoundSummaryDto> roundSummaries = rounds.stream()
                .map(r -> {
                    Person target = persons.get(r.getTargetPersonId());
                    Person selected = r.getSelectedPersonId() != null ? persons.get(r.getSelectedPersonId()) : null;
                    return new GameResultResponse.RoundSummaryDto(
                            r.getRoundNumber(),
                            target != null ? target.getFullName() : null,
                            selected != null ? selected.getFullName() : null,
                            Boolean.TRUE.equals(r.getCorrect()),
                            r.getReactionTimeMillis() == null ? 0 : r.getReactionTimeMillis()
                    );
                })
                .toList();

        double correctPct = rounds.isEmpty() ? 0 : (correct * 100.0) / rounds.size();
        double incorrectPct = rounds.isEmpty() ? 0 : (incorrect * 100.0) / rounds.size();

        return new GameResultResponse(
                gameId,
                game.getTotalRounds(),
                correct,
                incorrect,
                correctPct,
                incorrectPct,
                totalTime,
                avgReaction,
                roundSummaries,
                game.getFinishedAt()
        );
    }

    private void validateFinished(Game game) {
        if (game.getStatus() != com.namegame.domain.enums.GameStatus.FINISHED) {
            throw new IllegalStateException("Game results are only available once the game is finished");
        }
    }
}
