-- Drop FK constraint from remission_guide to recipient
ALTER TABLE remission_guide DROP FOREIGN KEY fk_rg_recipient;
ALTER TABLE remission_guide DROP COLUMN recipient_id;
ALTER TABLE remission_guide ADD COLUMN client_id BIGINT AFTER id;

-- Drop recipient table (no records, table no longer needed)
DROP TABLE IF EXISTS recipient;
