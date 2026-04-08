package com.namegame.application.usecase;

import com.namegame.application.dto.GameResultResponse;
import com.namegame.domain.enums.GameStatus;
import com.namegame.domain.enums.Gender;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetGameResultsUseCaseTest {

    @Mock
    private GameRepositoryPort gameRepository;

    @Mock
    private PersonRepositoryPort personRepository;

    @InjectMocks
    private GetGameResultsUseCase useCase;

    private UUID gameId;
    private Game mockGame;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        mockGame = mock(Game.class);
    }

    @Test
    @DisplayName("Should return GameResultResponse with correct calculations using Record accessors")
    void getResultResponse_Success() {
        // 1. Setup Personas
        UUID p1Id = UUID.randomUUID();
        UUID p2Id = UUID.randomUUID();
        Instant now = Instant.now();

        Person p1 = new Person(p1Id, "John", "Doe", Gender.MALE, "url1", true, now);
        Person p2 = new Person(p2Id, "Jane", "Smith", Gender.FEMALE, "url2", true, now);

        // 2. Mock Rondas (2 rondas: una correcta, una incorrecta)
        Round r1 = mock(Round.class);
        when(r1.getRoundNumber()).thenReturn(1);
        when(r1.getTargetPersonId()).thenReturn(p1Id);
        when(r1.getSelectedPersonId()).thenReturn(p1Id); // Correcto
        when(r1.getCorrect()).thenReturn(true);
        when(r1.getReactionTimeMillis()).thenReturn(1000L);

        Round r2 = mock(Round.class);
        when(r2.getRoundNumber()).thenReturn(2);
        when(r2.getTargetPersonId()).thenReturn(p2Id);
        when(r2.getSelectedPersonId()).thenReturn(p1Id); // Incorrecto
        when(r2.getCorrect()).thenReturn(false);
        when(r2.getReactionTimeMillis()).thenReturn(3000L);

        // 3. Setup Game
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getStatus()).thenReturn(GameStatus.FINISHED);
        when(mockGame.getRounds()).thenReturn(List.of(r1, r2));
        when(mockGame.getTotalRounds()).thenReturn(2);
        when(mockGame.getFinishedAt()).thenReturn(now); // Coincide con el tipo Instant del Record

        when(personRepository.findAllByIds(anyList())).thenReturn(List.of(p1, p2));

        // 4. Ejecución
        GameResultResponse result = useCase.getResultResponse(gameId);

        // 5. Validaciones usando los métodos de acceso del Record
        assertThat(result.gameId()).isEqualTo(gameId);
        assertThat(result.totalRounds()).isEqualTo(2);
        assertThat(result.correctAnswers()).isEqualTo(1);
        assertThat(result.incorrectAnswers()).isEqualTo(1);
        assertThat(result.correctPercentage()).isEqualTo(50.0);
        assertThat(result.incorrectPercentage()).isEqualTo(50.0);
        assertThat(result.totalTimeMillis()).isEqualTo(4000L);
        assertThat(result.averageReactionTimeMillis()).isEqualTo(2000.0);
        assertThat(result.finishedAt()).isEqualTo(now);

        // Validar RoundSummaryDto (también Record)
        assertThat(result.rounds()).hasSize(2);
        GameResultResponse.RoundSummaryDto summary1 = result.rounds().get(0);
        assertThat(summary1.targetName()).isEqualTo("John Doe");
        assertThat(summary1.selectedName()).isEqualTo("John Doe");
        assertThat(summary1.correct()).isTrue();
        assertThat(summary1.reactionTimeMillis()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when game is NOT_FINISHED")
    void getResultResponse_NotFinished_ThrowsException() {
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getStatus()).thenReturn(GameStatus.IN_PROGRESS);

        assertThatThrownBy(() -> useCase.getResultResponse(gameId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Game results are only available once the game is finished");
    }

    @Test
    @DisplayName("Should handle missing data in rounds (null values)")
    void getResultResponse_HandleNulls() {
        UUID p1Id = UUID.randomUUID();
        Person p1 = new Person(p1Id, "Target", "Only", Gender.MALE, "url", true, Instant.now());

        Round r1 = mock(Round.class);
        when(r1.getTargetPersonId()).thenReturn(p1Id);
        when(r1.getSelectedPersonId()).thenReturn(null);
        when(r1.getCorrect()).thenReturn(null);
        when(r1.getReactionTimeMillis()).thenReturn(null);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(mockGame));
        when(mockGame.getStatus()).thenReturn(GameStatus.FINISHED);
        when(mockGame.getRounds()).thenReturn(List.of(r1));
        when(personRepository.findAllByIds(anyList())).thenReturn(List.of(p1));

        GameResultResponse result = useCase.getResultResponse(gameId);

        assertThat(result.rounds().get(0).selectedName()).isNull();
        assertThat(result.rounds().get(0).correct()).isFalse(); // Boolean.TRUE.equals(null) es false
        assertThat(result.totalTimeMillis()).isZero();
    }
}