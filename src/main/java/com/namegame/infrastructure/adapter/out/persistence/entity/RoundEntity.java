package com.namegame.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rounds",
        uniqueConstraints = @UniqueConstraint(name = "uq_game_round", columnNames = {"game_id", "round_number"}))
public class RoundEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "target_person_id", nullable = false)
    private UUID targetPersonId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "option_person_ids", nullable = false, columnDefinition = "uuid[]")
    private List<UUID> optionPersonIds;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "selected_person_id")
    private UUID selectedPersonId;

    @Column(name = "correct")
    private Boolean correct;

    @Column(name = "presented_at")
    private Instant presentedAt;

    @Column(name = "answered_at")
    private Instant answeredAt;

    @Column(name = "reaction_time_millis")
    private Long reactionTimeMillis;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        if (this.status == null) this.status = "PENDING";
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public GameEntity getGame() { return game; }
    public void setGame(GameEntity game) { this.game = game; }

    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }

    public UUID getTargetPersonId() { return targetPersonId; }
    public void setTargetPersonId(UUID targetPersonId) { this.targetPersonId = targetPersonId; }

    public List<UUID> getOptionPersonIds() { return optionPersonIds; }
    public void setOptionPersonIds(List<UUID> optionPersonIds) { this.optionPersonIds = optionPersonIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
