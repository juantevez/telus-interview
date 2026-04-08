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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/games")
public class GameController {

    private final StartGameUseCase startGame;
    private final GetRoundUseCase getRound;
    private final SubmitAnswerUseCase submitAnswer;
    private final GetGameResultsUseCase getResults;
    private final GameRestMapper mapper;

    public GameController(StartGameUseCase startGame, GetRoundUseCase getRound,
                          SubmitAnswerUseCase submitAnswer, GetGameResultsUseCase getResults,
                          GameRestMapper mapper) {
        this.startGame = startGame;
        this.getRound = getRound;
        this.submitAnswer = submitAnswer;
        this.getResults = getResults;
        this.mapper = mapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> startGame(@RequestBody StartGameRequest request) {
        Game game = startGame.startGame(request.totalRounds(), request.facesPerRound());
        return mapper.toStartGameResponse(game);
    }

    @GetMapping("/{gameId}/rounds/{roundNumber}")
    public RoundResponse getRound(@PathVariable UUID gameId, @PathVariable int roundNumber) {
        return getRound.getRoundResponse(gameId, roundNumber);
    }

    @PostMapping("/{gameId}/rounds/{roundNumber}/answer")
    public AnswerResultResponse submitAnswer(@PathVariable UUID gameId,
                                             @PathVariable int roundNumber,
                                             @RequestBody SubmitAnswerRequest request) {
        return submitAnswer.submitAnswerWithDetails(
                gameId, roundNumber, request.selectedPersonId(), request.clientTimestamp());
    }

    @GetMapping("/{gameId}/results")
    public GameResultResponse getResults(@PathVariable UUID gameId) {
        return getResults.getResultResponse(gameId);
    }

    public record StartGameRequest(int totalRounds, int facesPerRound) {}

    public record SubmitAnswerRequest(UUID selectedPersonId, Instant clientTimestamp) {}
}
