package com.namegame.application.usecase;

import com.namegame.domain.exception.InsufficientPeopleException;
import com.namegame.domain.model.Game;
import com.namegame.domain.model.Person;
import com.namegame.domain.model.Round;
import com.namegame.domain.port.in.StartGamePort;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class StartGameUseCase implements StartGamePort {

    private final GameRepositoryPort gameRepository;
    private final PersonRepositoryPort personRepository;

    public StartGameUseCase(GameRepositoryPort gameRepository, PersonRepositoryPort personRepository) {
        this.gameRepository = gameRepository;
        this.personRepository = personRepository;
    }

    @Override
    public Game startGame(int totalRounds, int facesPerRound) {
        long activeCount = personRepository.countActive();
        if (activeCount < facesPerRound) {
            throw new InsufficientPeopleException(activeCount, facesPerRound);
        }

        List<Round> rounds = buildRounds(totalRounds, facesPerRound);
        Game game = new Game(UUID.randomUUID(), totalRounds, facesPerRound, rounds, Instant.now());
        return gameRepository.save(game);
    }

    private static final Random RANDOM = new Random();

    private List<Round> buildRounds(int totalRounds, int facesPerRound) {
        List<Round> rounds = new ArrayList<>();
        for (int i = 1; i <= totalRounds; i++) {
            List<Person> persons = personRepository.findRandomActive(facesPerRound);
            Person target = persons.get(RANDOM.nextInt(persons.size()));
            List<UUID> optionIds = persons.stream().map(Person::getId).toList();

            rounds.add(new Round(UUID.randomUUID(), i, target.getId(), optionIds, null));
        }
        return rounds;
    }
}
