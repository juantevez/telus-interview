-- Persons are now sourced from the WillowTree external API.
-- Remove FK constraints from rounds that referenced the local persons table.
ALTER TABLE rounds DROP CONSTRAINT IF EXISTS rounds_target_person_id_fkey;
ALTER TABLE rounds DROP CONSTRAINT IF EXISTS rounds_selected_person_id_fkey;

DROP TABLE IF EXISTS persons;
