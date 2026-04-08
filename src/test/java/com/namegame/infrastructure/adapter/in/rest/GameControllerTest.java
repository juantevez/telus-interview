package com.namegame.infrastructure.adapter.in.rest;

import com.namegame.application.dto.AnswerResultResponse;
import com.namegame.application.dto.GameResultResponse;
import com.namegame.application.dto.RoundResponse;
import com.namegame.application.usecase.GetGameResultsUseCase;
import com.namegame.application.usecase.GetRoundUseCase;
import com.namegame.application.usecase.StartGameUseCase;
import com.namegame.application.usecase.SubmitAnswerUseCase;
import com.namegame.domain.model.Game;
import com.namegame.infrastructure.adapter.in.rest.mapper.GameRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameControllerTest {

    @Mock private StartGameUseCase startGame;
    @Mock private GetRoundUseCase getRound;
    @Mock private SubmitAnswerUseCase submitAnswer;
    @Mock private GetGameResultsUseCase getResults;
    @Mock private GameRestMapper mapper;

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(startGame, getRound, submitAnswer, getResults, mapper);
    }

    @Test
    void startGame_ShouldReturnMappedResponse() {
        // Arrange
        var request = new GameController.StartGameRequest(10, 4);
        Game mockGame = mock(Game.class);
        Map<String, Object> expectedResponse = Map.of("gameId", UUID.randomUUID());

        when(startGame.startGame(10, 4)).thenReturn(mockGame);
        when(mapper.toStartGameResponse(mockGame)).thenReturn(expectedResponse);

        // Act
        Map<String, Object> response = controller.startGame(request);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(startGame).startGame(10, 4);
    }

    @Test
    void getRound_ShouldReturnResponseFromUseCase() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        int roundNumber = 1;
        RoundResponse expectedResponse = mock(RoundResponse.class);

        when(getRound.getRoundResponse(gameId, roundNumber)).thenReturn(expectedResponse);

        // Act
        RoundResponse response = controller.getRound(gameId, roundNumber);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(getRound).getRoundResponse(gameId, roundNumber);
    }

    @Test
    void submitAnswer_ShouldReturnDetailedResponse() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        UUID selectedId = UUID.randomUUID();
        Instant now = Instant.now();
        int roundNumber = 2;

        var request = new GameController.SubmitAnswerRequest(selectedId, now);
        AnswerResultResponse expectedResponse = mock(AnswerResultResponse.class);

        when(submitAnswer.submitAnswerWithDetails(gameId, roundNumber, selectedId, now))
                .thenReturn(expectedResponse);

        // Act
        AnswerResultResponse response = controller.submitAnswer(gameId, roundNumber, request);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(submitAnswer).submitAnswerWithDetails(gameId, roundNumber, selectedId, now);
    }

    @Test
    void getResults_ShouldReturnResponseFromUseCase() {
        // Arrange
        UUID gameId = UUID.randomUUID();
        GameResultResponse expectedResponse = mock(GameResultResponse.class);

        when(getResults.getResultResponse(gameId)).thenReturn(expectedResponse);

        // Act
        GameResultResponse response = controller.getResults(gameId);

        // Assert
        assertThat(response).isEqualTo(expectedResponse);
        verify(getResults).getResultResponse(gameId);
    }
}
