CREATE TABLE rounds (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id              UUID        NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    round_number         INTEGER     NOT NULL CHECK (round_number > 0),
    target_person_id     UUID        NOT NULL REFERENCES persons(id),
    option_person_ids    UUID[]      NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'ANSWERED')),
    selected_person_id   UUID        REFERENCES persons(id),
    correct              BOOLEAN,
    presented_at         TIMESTAMPTZ,
    answered_at          TIMESTAMPTZ,
    reaction_time_millis BIGINT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_game_round UNIQUE (game_id, round_number)
);

CREATE INDEX idx_rounds_game_id ON rounds (game_id);
CREATE INDEX idx_rounds_status  ON rounds (status);
