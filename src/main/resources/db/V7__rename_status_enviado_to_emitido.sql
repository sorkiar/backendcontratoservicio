-- V7: Rename status ENVIADO → EMITIDO across all SUNAT document tables

UPDATE sale_document     SET status = 'EMITIDO' WHERE status = 'ENVIADO';
UPDATE credit_debit_note SET status = 'EMITIDO' WHERE status = 'ENVIADO';
UPDATE remission_guide   SET status = 'EMITIDO' WHERE status = 'ENVIADO';
UPDATE documento         SET estado = 'EMITIDO' WHERE estado = 'ENVIADO';
