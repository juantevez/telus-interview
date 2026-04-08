CREATE TABLE persons (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    gender          VARCHAR(10)  NOT NULL CHECK (gender IN ('MALE', 'FEMALE')),
    photo_url       VARCHAR(500) NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_persons_active ON persons (active) WHERE active = TRUE;
CREATE INDEX idx_persons_gender ON persons (gender);
