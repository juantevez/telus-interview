package com.namegame.domain.model;

import com.namegame.domain.enums.GameStatus;
import com.namegame.domain.exception.GameAlreadyFinishedException;
import com.namegame.domain.exception.RoundNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Game {

    private UUID id;
    private Integer totalRounds;
    private Integer facesPerRound;
    private GameStatus status;
    private List<Round> rounds;
    private Instant createdAt;
    private Instant finishedAt;
    private Long version;

    public Game() {}

    public Game(UUID id, Integer totalRounds, Integer facesPerRound,
                List<Round> rounds, Instant createdAt) {
        this.id = id;
        this.totalRounds = totalRounds;
        this.facesPerRound = facesPerRound;
        this.status = GameStatus.IN_PROGRESS;
        this.rounds = rounds;
        this.createdAt = createdAt;
    }

    public Round getCurrentRound() {
        return rounds.stream()
                .filter(r -> !r.isAnswered())
                .findFirst()
                .orElseThrow(() -> new RoundNotFoundException(id, -1));
    }

    public Round getRoundByNumber(int roundNumber) {
        return rounds.stream()
                .filter(r -> r.getRoundNumber() == roundNumber)
                .findFirst()
                .orElseThrow(() -> new RoundNotFoundException(id, roundNumber));
    }

    public void finish() {
        if (this.status == GameStatus.FINISHED) {
            throw new GameAlreadyFinishedException(id);
        }
        this.status = GameStatus.FINISHED;
        this.finishedAt = Instant.now();
    }

    public boolean isLastRound(int roundNumber) {
        return roundNumber == totalRounds;
    }

    public GameResult calculateResult() {
        List<Round> answered = rounds.stream().filter(Round::isAnswered).toList();

        int correct = (int) answered.stream().filter(r -> Boolean.TRUE.equals(r.getCorrect())).count();
        int incorrect = answered.size() - correct;
        long totalTime = answered.stream()
                .mapToLong(r -> r.getReactionTimeMillis() == null ? 0 : r.getReactionTimeMillis())
                .sum();
        double avgReaction = answered.isEmpty() ? 0 :
                answered.stream()
                        .mapToLong(r -> r.getReactionTimeMillis() == null ? 0 : r.getReactionTimeMillis())
                        .average()
                        .orElse(0);

        List<RoundAnswer> details = answered.stream()
                .map(r -> new RoundAnswer(
                        r.getRoundNumber(),
                        null,
                        null,
                        Boolean.TRUE.equals(r.getCorrect()),
                        r.getReactionTimeMillis() == null ? 0 : r.getReactionTimeMillis()
                ))
                .toList();

        double correctPct = totalRounds == 0 ? 0 : (correct * 100.0) / totalRounds;
        double incorrectPct = totalRounds == 0 ? 0 : (incorrect * 100.0) / totalRounds;

        return new GameResult(id, totalRounds, correct, incorrect,
                correctPct, incorrectPct, details, totalTime, avgReaction);
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getTotalRounds() { return totalRounds; }
    public void setTotalRounds(Integer totalRounds) { this.totalRounds = totalRounds; }

    public Integer getFacesPerRound() { return facesPerRound; }
    public void setFacesPerRound(Integer facesPerRound) { this.facesPerRound = facesPerRound; }

    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }

    public List<Round> getRounds() { return rounds; }
    public void setRounds(List<Round> rounds) { this.rounds = rounds; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
