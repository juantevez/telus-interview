package com.namegame.domain.model;

import com.namegame.domain.enums.RoundStatus;
import com.namegame.domain.exception.RoundAlreadyAnsweredException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class Round {

    private UUID id;
    private Integer roundNumber;
    private UUID targetPersonId;
    private List<UUID> optionPersonIds;
    private RoundStatus status;
    private UUID selectedPersonId;
    private Boolean correct;
    private Instant presentedAt;
    private Instant answeredAt;
    private Long reactionTimeMillis;

    public Round() {}

    public Round(UUID id, Integer roundNumber, UUID targetPersonId,
                 List<UUID> optionPersonIds, Instant presentedAt) {
        this.id = id;
        this.roundNumber = roundNumber;
        this.targetPersonId = targetPersonId;
        this.optionPersonIds = optionPersonIds;
        this.status = RoundStatus.PENDING;
        this.presentedAt = presentedAt;
    }

    public void submitAnswer(UUID selectedPersonId, Instant answeredAt) {
        if (this.status == RoundStatus.ANSWERED) {
            throw new RoundAlreadyAnsweredException(roundNumber);
        }
        this.selectedPersonId = selectedPersonId;
        this.answeredAt = answeredAt;
        this.reactionTimeMillis = Duration.between(presentedAt, answeredAt).toMillis();
        this.correct = targetPersonId.equals(selectedPersonId);
        this.status = RoundStatus.ANSWERED;
    }

    public boolean isAnswered() {
        return RoundStatus.ANSWERED == status;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

    public UUID getTargetPersonId() { return targetPersonId; }
    public void setTargetPersonId(UUID targetPersonId) { this.targetPersonId = targetPersonId; }

    public List<UUID> getOptionPersonIds() { return optionPersonIds; }
    public void setOptionPersonIds(List<UUID> optionPersonIds) { this.optionPersonIds = optionPersonIds; }

    public RoundStatus getStatus() { return status; }
    public void setStatus(RoundStatus status) { this.status = status; }

    public UUID getSelectedPersonId() { return selectedPersonId; }
    public void setSelectedPersonId(UUID selectedPersonId) { this.selectedPersonId = selectedPersonId; }

    public Boolean getCorrect() { return correct; }
    public void setCorrect(Boolean correct) { this.correct = correct; }

    public Instant getPresentedAt() { return presentedAt; }
    public void setPresentedAt(Instant presentedAt) { this.presentedAt = presentedAt; }

    public Instant getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(Instant answeredAt) { this.answeredAt = answeredAt; }

    public Long getReactionTimeMillis() { return reactionTimeMillis; }
    public void setReactionTimeMillis(Long reactionTimeMillis) { this.reactionTimeMillis = reactionTimeMillis; }
}
