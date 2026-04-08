package com.namegame.application.usecase;

import com.namegame.domain.enums.GameStatus;
import com.namegame.domain.enums.Gender;
import com.namegame.domain.exception.InsufficientPeopleException;
import com.namegame.domain.model.Game;
import com.namegame.domain.model.Person;
import com.namegame.domain.model.Round;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartGameUseCaseTest {

    @Mock
    private GameRepositoryPort gameRepository;

    @Mock
    private PersonRepositoryPort personRepository;

    @InjectMocks
    private StartGameUseCase useCase;

    @Test
    @DisplayName("Should successfully start a game and save it when sufficient people are active")
    void startGame_Success() {
        // GIVEN
        int totalRounds = 3;
        int facesPerRound = 6;

        // Mock de conteo de personas activas
        when(personRepository.countActive()).thenReturn(10L);

        // Mock de personas aleatorias (se llamará una vez por cada ronda)
        Person person1 = new Person(UUID.randomUUID(), "Name1", "Last1", Gender.MALE, "url1", true, Instant.now());
        Person person2 = new Person(UUID.randomUUID(), "Name2", "Last2", Gender.FEMALE, "url2", true, Instant.now());
        List<Person> mockPersons = List.of(person1, person2);

        // En cada iteración del bucle buildRounds devolvemos una lista de personas
        when(personRepository.findRandomActive(facesPerRound)).thenReturn(mockPersons);

        // El repositorio guarda y devuelve el objeto (usamos returnsFirstArg por simplicidad)
        when(gameRepository.save(any(Game.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        Game createdGame = useCase.startGame(totalRounds, facesPerRound);

        // THEN
        assertThat(createdGame).isNotNull();
        assertThat(createdGame.getTotalRounds()).isEqualTo(totalRounds);
        assertThat(createdGame.getFacesPerRound()).isEqualTo(facesPerRound);
        assertThat(createdGame.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(createdGame.getRounds()).hasSize(totalRounds);

        // Verificar que la primera ronda tiene los datos correctos del mock
        Round firstRound = createdGame.getRounds().get(0);
        assertThat(firstRound.getTargetPersonId()).isEqualTo(person1.getId());
        assertThat(firstRound.getOptionPersonIds()).contains(person1.getId(), person2.getId());

        // Verificar interacciones
        verify(personRepository, times(1)).countActive();
        verify(personRepository, times(totalRounds)).findRandomActive(facesPerRound);
        verify(gameRepository, times(1)).save(any(Game.class));
    }

    @Test
    @DisplayName("Should throw InsufficientPeopleException when active count is less than facesPerRound")
    void startGame_InsufficientPeople_ThrowsException() {
        // GIVEN
        int totalRounds = 5;
        int facesPerRound = 10;
        when(personRepository.countActive()).thenReturn(5L); // Solo hay 5, se requieren 10

        // WHEN & THEN
        assertThatThrownBy(() -> useCase.startGame(totalRounds, facesPerRound))
                .isInstanceOf(InsufficientPeopleException.class);

        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should verify that Game is created with unique IDs and current timestamp")
    void startGame_VerifyInternalData() {
        // GIVEN
        when(personRepository.countActive()).thenReturn(20L);
        Person p = new Person(UUID.randomUUID(), "A", "B", Gender.FEMALE, "url", true, Instant.now());
        when(personRepository.findRandomActive(anyInt())).thenReturn(List.of(p));

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);

        // WHEN
        useCase.startGame(1, 1);

        // THEN
        verify(gameRepository).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();

        assertThat(savedGame.getId()).isNotNull();
        assertThat(savedGame.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(savedGame.getRounds().get(0).getRoundNumber()).isEqualTo(1);
    }
}