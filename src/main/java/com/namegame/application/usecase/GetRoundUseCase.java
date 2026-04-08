package com.namegame.application.usecase;

import com.namegame.application.dto.RoundResponse;
import com.namegame.domain.exception.GameNotFoundException;
import com.namegame.domain.model.Game;
import com.namegame.domain.model.Person;
import com.namegame.domain.model.Round;
import com.namegame.domain.port.in.GetRoundPort;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GetRoundUseCase implements GetRoundPort {

    private final GameRepositoryPort gameRepository;
    private final PersonRepositoryPort personRepository;

    public GetRoundUseCase(GameRepositoryPort gameRepository,
                           PersonRepositoryPort personRepository) {
        this.gameRepository = gameRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Round getRound(UUID gameId, int roundNumber) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        Round round = game.getRoundByNumber(roundNumber);

        // Set presentedAt idempotently — only on first access
        if (round.getPresentedAt() == null) {
            round.setPresentedAt(Instant.now());
            gameRepository.save(game);
        }

        return round;
    }

    public RoundResponse getRoundResponse(UUID gameId, int roundNumber) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        Round round = game.getRoundByNumber(roundNumber);

        if (round.getPresentedAt() == null) {
            round.setPresentedAt(Instant.now());
            gameRepository.save(game);
        }

        Map<UUID, Person> persons = personRepository.findAllByIds(round.getOptionPersonIds())
                .stream().collect(Collectors.toMap(Person::getId, Function.identity()));

        Person target = persons.get(round.getTargetPersonId());

        List<RoundResponse.FaceDto> faces = round.getOptionPersonIds().stream()
                .map(persons::get)
                .map(p -> new RoundResponse.FaceDto(p.getId(), p.getPhotoUrl()))
                .toList();

        return new RoundResponse(
                gameId,
                round.getRoundNumber(),
                game.getTotalRounds(),
                target.getFullName(),
                faces,
                round.getStatus().name(),
                round.getPresentedAt()
        );
    }
}
