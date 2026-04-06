-- Agrega la columna carrier_ruc que la entidad RemissionGuide espera encontrar.
-- La migración V1 creó carrier_doc_type y carrier_doc_number en su lugar,
-- generando un desajuste con la entidad JPA.
ALTER TABLE remission_guide
    ADD COLUMN IF NOT EXISTS carrier_ruc VARCHAR(20) AFTER minor_vehicle_transfer;
