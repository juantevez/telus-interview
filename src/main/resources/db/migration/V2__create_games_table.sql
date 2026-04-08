CREATE TABLE games (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    total_rounds    INTEGER      NOT NULL CHECK (total_rounds > 0),
    faces_per_round INTEGER      NOT NULL CHECK (faces_per_round >= 2),
    status          VARCHAR(20)  NOT NULL DEFAULT 'CREATED'
                    CHECK (status IN ('CREATED', 'IN_PROGRESS', 'FINISHED')),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    finished_at     TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_games_status ON games (status);
