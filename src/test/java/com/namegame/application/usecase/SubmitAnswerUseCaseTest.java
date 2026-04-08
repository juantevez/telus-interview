package com.namegame.application.usecase;

import com.namegame.application.dto.AnswerResultResponse;
import com.namegame.domain.enums.GameStatus;
import com.namegame.domain.enums.Gender;
import com.namegame.domain.exception.GameNotFoundException;
import com.namegame.domain.model.Game;
import com.namegame.domain.model.Person;
import com.namegame.domain.model.Round;
import com.namegame.domain.port.out.GameRepositoryPort;
import com.namegame.domain.port.out.PersonRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubmitAnswerUseCaseTest {

    @Mock
    private GameRepositoryPort gameRepository;

    @Mock
    private PersonRepositoryPort personRepository;

    @InjectMocks
    private SubmitAnswerUseCase useCase;

    private UUID gameId;
    private UUID targetId;
    private UUID selectedId;
    private Game game;
    private Round round;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        targetId = UUID.randomUUID();
        selectedId = targetId; // Respuesta correcta por defecto

        // Creamos una ronda real para que su lógica interna funcione
        round = new Round(UUID.randomUUID(), 1, targetId, List.of(targetId, UUID.randomUUID()), Instant.now());

        // Creamos un juego real
        game = new Game(gameId, 2, 6, new ArrayList<>(List.of(round)), Instant.now());
    }

    @Test
    @DisplayName("Should submit correct answer and save game")
    void submitAnswer_Correct_Success() {
        // GIVEN
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // WHEN
        Round result = useCase.submitAnswer(gameId, 1, selectedId, Instant.now());

        // THEN
        assertThat(result.getCorrect()).isTrue();
        assertThat(result.getStatus().name()).isEqualTo("ANSWERED");
        assertThat(result.getReactionTimeMillis()).isNotNull();
        verify(gameRepository).save(game);
    }

    @Test
    @DisplayName("Should finish game if it is the last round")
    void submitAnswer_LastRound_FinishesGame() {
        // GIVEN
        game.setTotalRounds(1); // Esta es la última ronda
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // WHEN
        useCase.submitAnswer(gameId, 1, selectedId, Instant.now());

        // THEN
        assertThat(game.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(game.getFinishedAt()).isNotNull();
        verify(gameRepository).save(game);
    }

    @Test
    @DisplayName("Should return AnswerResultResponse with full details and next round info")
    void submitAnswerWithDetails_Success() {
        // GIVEN
        game.setTotalRounds(5); // Hay más rondas
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Person target = new Person(targetId, "John", "Doe", Gender.MALE, "url1", true, Instant.now());
        when(personRepository.findAllByIds(anyList())).thenReturn(List.of(target));

        // WHEN
        AnswerResultResponse response = useCase.submitAnswerWithDetails(gameId, 1, selectedId, Instant.now());

        // THEN
        assertThat(response.roundNumber()).isEqualTo(1);
        assertThat(response.correct()).isTrue();
        assertThat(response.targetName()).isEqualTo("John Doe");
        assertThat(response.hasNextRound()).isTrue();
        assertThat(response.nextRoundNumber()).isEqualTo(2);

        verify(personRepository).findAllByIds(argThat(list -> list.contains(targetId)));
    }

    @Test
    @DisplayName("Should return hasNextRound false when finishing the game")
    void submitAnswerWithDetails_NoNextRound() {
        // GIVEN
        game.setTotalRounds(1);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        Person target = new Person(targetId, "John", "Doe", Gender.MALE, "url1", true, Instant.now());
        when(personRepository.findAllByIds(anyList())).thenReturn(List.of(target));

        // WHEN
        AnswerResultResponse response = useCase.submitAnswerWithDetails(gameId, 1, selectedId, Instant.now());

        // THEN
        assertThat(response.hasNextRound()).isFalse();
        assertThat(response.nextRoundNumber()).isNull();
    }

    @Test
    @DisplayName("Should throw GameNotFoundException if game does not exist")
    void submitAnswer_NotFound_ThrowsException() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.submitAnswer(gameId, 1, selectedId, Instant.now()))
                .isInstanceOf(GameNotFoundException.class);
    }
}
