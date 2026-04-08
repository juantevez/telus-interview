package com.namegame.infrastructure.adapter.in.rest.mapper;

import com.namegame.application.dto.StartGameCommand;
import com.namegame.application.dto.SubmitAnswerCommand;
import com.namegame.domain.enums.GameStatus;
import com.namegame.domain.model.Game;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GameRestMapperTest {

    private GameRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new GameRestMapper();
    }

    @Test
    void toStartCommand_ShouldMapCorrectly() {
        StartGameCommand command = mapper.toStartCommand(10, 4);

        assertThat(command.totalRounds()).isEqualTo(10);
        assertThat(command.facesPerRound()).isEqualTo(4);
    }

    @Test
    void toStartGameResponse_ShouldMapGameDetails() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        Instant now = Instant.now();
        Game game = mock(Game.class);

        when(game.getId()).thenReturn(gameId);
        when(game.getTotalRounds()).thenReturn(5);
        when(game.getFacesPerRound()).thenReturn(6);
        when(game.getStatus()).thenReturn(GameStatus.IN_PROGRESS);
        when(game.getCreatedAt()).thenReturn(now);

        // Act
        Map<String, Object> response = mapper.toStartGameResponse(game);

        // Assert
        assertAll("Comprobar todos los campos del Map",
                () -> assertThat(response.get("gameId")).isEqualTo(gameId),
                () -> assertThat(response.get("totalRounds")).isEqualTo(5),
                () -> assertThat(response.get("facesPerRound")).isEqualTo(6),
                () -> assertThat(response.get("status")).isEqualTo("IN_PROGRESS"),
                () -> assertThat(response.get("currentRound")).isEqualTo(1),
                () -> assertThat(response.get("createdAt")).isEqualTo(now)
        );
    }

    @Test
    void toSubmitCommand_ShouldMapCorrectly() {
        UUID gameId = UUID.randomUUID();
        UUID personId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        SubmitAnswerCommand command = mapper.toSubmitCommand(gameId, 2, personId, timestamp);

        assertAll(
                () -> assertThat(command.gameId()).isEqualTo(gameId),
                () -> assertThat(command.roundNumber()).isEqualTo(2),
                () -> assertThat(command.selectedPersonId()).isEqualTo(personId),
                () -> assertThat(command.clientTimestamp()).isEqualTo(timestamp)
        );
    }
}
