-- ============================================================
-- V1__new_modules.sql
-- Nuevos módulos: Productos, Servicios, Destinatarios, Ventas,
-- NC/ND, Guías de Remisión, Comprobantes, Reportes
-- Base de datos: telecomunicaciones
-- Ejecutar manualmente antes de arrancar la aplicación
-- ============================================================

USE telecomunicaciones;

-- ============================================================
-- TABLAS MAESTRAS (catálogos)
-- ============================================================

CREATE TABLE IF NOT EXISTS category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME,
    created_by  VARCHAR(50),
    updated_at  DATETIME,
    updated_by  VARCHAR(50),
    deleted_at  DATETIME,
    deleted_by  VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS service_category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME,
    created_by  VARCHAR(50),
    updated_at  DATETIME,
    updated_by  VARCHAR(50),
    deleted_at  DATETIME,
    deleted_by  VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS charge_unit
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(20) NOT NULL UNIQUE,
    name       VARCHAR(50) NOT NULL,
    status     TINYINT     NOT NULL DEFAULT 1,
    created_at DATETIME,
    created_by VARCHAR(50),
    updated_at DATETIME,
    updated_by VARCHAR(50),
    deleted_at DATETIME,
    deleted_by VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS unit_measure
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(10) NOT NULL UNIQUE,
    code_sunat VARCHAR(4)  NOT NULL,
    name       VARCHAR(50) NOT NULL,
    symbol     VARCHAR(10),
    status     TINYINT     NOT NULL DEFAULT 1,
    created_at DATETIME,
    created_by VARCHAR(50),
    updated_at DATETIME,
    updated_by VARCHAR(50),
    deleted_at DATETIME,
    deleted_by VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS person_type
(
    id          TINYINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(150),
    status      TINYINT     NOT NULL DEFAULT 1,
    created_at  DATETIME,
    created_by  VARCHAR(50),
    updated_at  DATETIME,
    updated_by  VARCHAR(50),
    deleted_at  DATETIME,
    deleted_by  VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS document_type
(
    id             TINYINT AUTO_INCREMENT PRIMARY KEY,
    person_type_id TINYINT     NOT NULL,
    name           VARCHAR(50) NOT NULL,
    length         TINYINT,
    description    VARCHAR(150),
    status         TINYINT     NOT NULL DEFAULT 1,
    created_at     DATETIME,
    created_by     VARCHAR(50),
    updated_at     DATETIME,
    updated_by     VARCHAR(50),
    deleted_at     DATETIME,
    deleted_by     VARCHAR(50),
    CONSTRAINT fk_dt_person_type FOREIGN KEY (person_type_id) REFERENCES person_type (id)
    );

CREATE TABLE IF NOT EXISTS document_type_sunat
(
    code       VARCHAR(3)   NOT NULL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME,
    created_by VARCHAR(50),
    updated_at DATETIME,
    updated_by VARCHAR(50),
    deleted_at DATETIME,
    deleted_by VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS payment_method
(
    id             TINYINT AUTO_INCREMENT PRIMARY KEY,
    code           VARCHAR(20)  NOT NULL UNIQUE,
    name           VARCHAR(100) NOT NULL,
    requires_proof TINYINT(1)   NOT NULL DEFAULT 0,
    status         TINYINT      NOT NULL DEFAULT 1,
    created_at     DATETIME,
    created_by     VARCHAR(50),
    updated_at     DATETIME,
    updated_by     VARCHAR(50),
    deleted_at     DATETIME,
    deleted_by     VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS credit_debit_note_type
(
    code          VARCHAR(4)   NOT NULL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    note_category VARCHAR(10)  NOT NULL COMMENT 'CREDIT o DEBIT',
    status        TINYINT      NOT NULL DEFAULT 1,
    created_at    DATETIME,
    created_by    VARCHAR(50),
    updated_at    DATETIME,
    updated_by    VARCHAR(50),
    deleted_at    DATETIME,
    deleted_by    VARCHAR(50)
    );

-- ============================================================
-- TABLAS OPERACIONALES
-- ============================================================

CREATE TABLE IF NOT EXISTS recipient
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    doc_type   VARCHAR(5)   NOT NULL,
    doc_number VARCHAR(20)  NOT NULL,
    name       VARCHAR(200) NOT NULL,
    address    VARCHAR(500),
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME,
    created_by VARCHAR(50),
    updated_at DATETIME,
    updated_by VARCHAR(50),
    deleted_at DATETIME,
    deleted_by VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS product
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku                 VARCHAR(50)    NOT NULL UNIQUE,
    name                VARCHAR(200)   NOT NULL,
    category_id         BIGINT,
    unit_measure_id     BIGINT,
    sale_price_pen      DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    estimated_cost_pen  DECIMAL(12, 2)          DEFAULT 0.00,
    brand               VARCHAR(100),
    model               VARCHAR(100),
    short_description   VARCHAR(500),
    technical_spec      TEXT,
    main_image_url      VARCHAR(255),
    technical_sheet_url VARCHAR(255),
    status              TINYINT        NOT NULL DEFAULT 1,
    created_at          DATETIME,
    created_by          VARCHAR(50),
    updated_at          DATETIME,
    updated_by          VARCHAR(50),
    deleted_at          DATETIME,
    deleted_by          VARCHAR(50),
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category (id),
    CONSTRAINT fk_product_unit_measure FOREIGN KEY (unit_measure_id) REFERENCES unit_measure (id)
    );

CREATE TABLE IF NOT EXISTS service
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku                    VARCHAR(50)    NOT NULL UNIQUE,
    name                   VARCHAR(200)   NOT NULL,
    service_category_id    BIGINT,
    charge_unit_id         BIGINT,
    price_pen              DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    estimated_time         VARCHAR(50),
    expected_delivery      VARCHAR(200),
    requires_materials     TINYINT(1)              DEFAULT 0,
    requires_specification TINYINT(1)              DEFAULT 0,
    includes_description   TEXT,
    excludes_description   TEXT,
    conditions             TEXT,
    short_description      TEXT,
    detailed_description   TEXT,
    image_url              VARCHAR(255),
    technical_sheet_url    VARCHAR(255),
    status                 TINYINT        NOT NULL DEFAULT 1,
    created_at             DATETIME,
    created_by             VARCHAR(50),
    updated_at             DATETIME,
    updated_by             VARCHAR(50),
    deleted_at             DATETIME,
    deleted_by             VARCHAR(50),
    CONSTRAINT fk_service_category FOREIGN KEY (service_category_id) REFERENCES service_category (id),
    CONSTRAINT fk_service_charge_unit FOREIGN KEY (charge_unit_id) REFERENCES charge_unit (id)
    );

CREATE TABLE IF NOT EXISTS sku_sequence
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    type           VARCHAR(10) NOT NULL UNIQUE,
    last_seq_value INT         NOT NULL DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS document_series
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type_sunat_code VARCHAR(3)   NOT NULL,
    series                   VARCHAR(4)   NOT NULL,
    current_sequence         INT UNSIGNED NOT NULL DEFAULT 0,
    status                   TINYINT      NOT NULL DEFAULT 1,
    created_at               DATETIME,
    created_by               VARCHAR(50),
    updated_at               DATETIME,
    updated_by               VARCHAR(50),
    deleted_at               DATETIME,
    deleted_by               VARCHAR(50),
    CONSTRAINT fk_ds_doc_type_sunat FOREIGN KEY (document_type_sunat_code) REFERENCES document_type_sunat (code)
    );

CREATE TABLE IF NOT EXISTS sale
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id       BIGINT         NOT NULL COMMENT 'Referencia a tabla clientes existente',
    sale_status     VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE',
    subtotal_amount DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    currency_code   VARCHAR(4)     NOT NULL DEFAULT 'PEN',
    tax_percentage  DECIMAL(5, 2)  NOT NULL DEFAULT 18.00,
    sale_date       DATETIME       NOT NULL,
    payment_type    VARCHAR(10)    NOT NULL DEFAULT 'CONTADO',
    purchase_order  VARCHAR(50),
    observations    TEXT,
    created_at      DATETIME,
    created_by      VARCHAR(50),
    updated_at      DATETIME,
    updated_by      VARCHAR(50),
    deleted_at      DATETIME,
    deleted_by      VARCHAR(50)
    );

CREATE TABLE IF NOT EXISTS sale_item
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id             BIGINT         NOT NULL,
    item_type           VARCHAR(20)    NOT NULL COMMENT 'PRODUCT o SERVICE',
    product_id          BIGINT,
    service_id          BIGINT,
    sku                 VARCHAR(50),
    description         VARCHAR(500),
    quantity            DECIMAL(14, 2) NOT NULL DEFAULT 1.00,
    unit_price          DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    discount_percentage DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    subtotal_amount     DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    tax_amount          DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    total_amount        DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    unit_measure_id     BIGINT,
    created_at          DATETIME,
    created_by          VARCHAR(50),
    updated_at          DATETIME,
    updated_by          VARCHAR(50),
    deleted_at          DATETIME,
    deleted_by          VARCHAR(50),
    CONSTRAINT fk_si_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT fk_si_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_si_service FOREIGN KEY (service_id) REFERENCES service (id),
    CONSTRAINT fk_si_unit_measure FOREIGN KEY (unit_measure_id) REFERENCES unit_measure (id)
    );

CREATE TABLE IF NOT EXISTS sale_payment
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id           BIGINT         NOT NULL,
    payment_method_id BIGINT         NOT NULL,
    amount            DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    change_amount     DECIMAL(14, 2)          DEFAULT 0.00,
    payment_date      DATETIME       NOT NULL,
    reference_number  VARCHAR(50),
    proof_url         VARCHAR(255),
    notes             TEXT,
    created_at        DATETIME,
    created_by        VARCHAR(50),
    updated_at        DATETIME,
    updated_by        VARCHAR(50),
    deleted_at        DATETIME,
    deleted_by        VARCHAR(50),
    CONSTRAINT fk_sp_sale FOREIGN KEY (sale_id) REFERENCES sale (id)
    );

CREATE TABLE IF NOT EXISTS sale_installment
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id    BIGINT         NOT NULL,
    amount     DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    due_date   DATE           NOT NULL,
    status     VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE',
    created_at DATETIME,
    created_by VARCHAR(50),
    updated_at DATETIME,
    updated_by VARCHAR(50),
    deleted_at DATETIME,
    deleted_by VARCHAR(50),
    CONSTRAINT fk_sin_sale FOREIGN KEY (sale_id) REFERENCES sale (id)
    );

CREATE TABLE IF NOT EXISTS sale_related_guide
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id      BIGINT      NOT NULL,
    guide_number VARCHAR(50) NOT NULL,
    CONSTRAINT fk_srg_sale FOREIGN KEY (sale_id) REFERENCES sale (id)
    );

CREATE TABLE IF NOT EXISTS sale_document
(
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id                  BIGINT      NOT NULL,
    document_type_sunat_code VARCHAR(3)  NOT NULL,
    document_series_id       BIGINT,
    series                   VARCHAR(4),
    sequence                 VARCHAR(8),
    issue_date               DATETIME,
    status                   VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    sunat_response_code      INT,
    sunat_message            TEXT,
    hash_code                VARCHAR(255),
    qr_code                  VARCHAR(255),
    xml_base64               LONGTEXT,
    cdr_base64               LONGTEXT,
    pdf_url                  VARCHAR(255),
    xml_url                  VARCHAR(500),
    cdr_url                  VARCHAR(500),
    created_at               DATETIME,
    created_by               VARCHAR(50),
    updated_at               DATETIME,
    updated_by               VARCHAR(50),
    deleted_at               DATETIME,
    deleted_by               VARCHAR(50),
    CONSTRAINT fk_sd_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT fk_sd_doc_type_sunat FOREIGN KEY (document_type_sunat_code) REFERENCES document_type_sunat (code),
    CONSTRAINT fk_sd_doc_series FOREIGN KEY (document_series_id) REFERENCES document_series (id)
    );

CREATE TABLE IF NOT EXISTS credit_debit_note
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id                     BIGINT         NOT NULL,
    original_document_id        BIGINT         NOT NULL,
    document_type_sunat_code    VARCHAR(3)     NOT NULL,
    document_series_id          BIGINT,
    series                      VARCHAR(4),
    sequence                    VARCHAR(8),
    issue_date                  DATETIME,
    credit_debit_note_type_code VARCHAR(4)     NOT NULL,
    reason                      VARCHAR(500),
    subtotal_amount             DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    tax_amount                  DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    total_amount                DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    tax_percentage              DECIMAL(5, 2)  NOT NULL DEFAULT 18.00,
    currency_code               VARCHAR(4)     NOT NULL DEFAULT 'PEN',
    status                      VARCHAR(20)    NOT NULL DEFAULT 'PENDIENTE',
    sunat_response_code         INT,
    sunat_message               TEXT,
    hash_code                   VARCHAR(255),
    qr_code                     VARCHAR(255),
    xml_base64                  LONGTEXT,
    cdr_base64                  LONGTEXT,
    pdf_url                     VARCHAR(255),
    xml_url                     VARCHAR(500),
    cdr_url                     VARCHAR(500),
    created_at                  DATETIME,
    created_by                  VARCHAR(50),
    updated_at                  DATETIME,
    updated_by                  VARCHAR(50),
    deleted_at                  DATETIME,
    deleted_by                  VARCHAR(50),
    CONSTRAINT fk_cdn_sale FOREIGN KEY (sale_id) REFERENCES sale (id),
    CONSTRAINT fk_cdn_orig_doc FOREIGN KEY (original_document_id) REFERENCES sale_document (id),
    CONSTRAINT fk_cdn_doc_type FOREIGN KEY (document_type_sunat_code) REFERENCES document_type_sunat (code),
    CONSTRAINT fk_cdn_doc_series FOREIGN KEY (document_series_id) REFERENCES document_series (id),
    CONSTRAINT fk_cdn_note_type FOREIGN KEY (credit_debit_note_type_code) REFERENCES credit_debit_note_type (code)
    );

CREATE TABLE IF NOT EXISTS credit_debit_note_item
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    credit_debit_note_id BIGINT         NOT NULL,
    item_type            VARCHAR(20)    NOT NULL COMMENT 'PRODUCT o SERVICE',
    product_id           BIGINT,
    service_id           BIGINT,
    description          VARCHAR(500),
    quantity             DECIMAL(14, 2) NOT NULL DEFAULT 1.00,
    unit_price           DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    discount_percentage  DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    subtotal_amount      DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    tax_amount           DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    total_amount         DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    unit_measure_id      BIGINT,
    created_at           DATETIME,
    created_by           VARCHAR(50),
    updated_at           DATETIME,
    updated_by           VARCHAR(50),
    deleted_at           DATETIME,
    deleted_by           VARCHAR(50),
    CONSTRAINT fk_cdni_note FOREIGN KEY (credit_debit_note_id) REFERENCES credit_debit_note (id),
    CONSTRAINT fk_cdni_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_cdni_service FOREIGN KEY (service_id) REFERENCES service (id),
    CONSTRAINT fk_cdni_unit_measure FOREIGN KEY (unit_measure_id) REFERENCES unit_measure (id)
    );

CREATE TABLE IF NOT EXISTS remission_guide
(
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_series_id          BIGINT,
    recipient_id                BIGINT,
    series                      VARCHAR(4),
    sequence                    VARCHAR(8),
    issue_date                  DATETIME,
    transfer_date               DATE,
    transfer_reason             VARCHAR(30),
    transfer_reason_description VARCHAR(255),
    transport_mode              VARCHAR(30),
    gross_weight                DECIMAL(14, 3),
    weight_unit                 VARCHAR(10),
    package_count               INT,
    origin_address              VARCHAR(500),
    origin_ubigeo               VARCHAR(10),
    destination_address         VARCHAR(500),
    destination_ubigeo          VARCHAR(10),
    minor_vehicle_transfer      TINYINT(1)           DEFAULT 0,
    carrier_doc_type            VARCHAR(5),
    carrier_doc_number          VARCHAR(20),
    carrier_name                VARCHAR(200),
    carrier_authorization_code  VARCHAR(50),
    observations                TEXT,
    status                      VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    sunat_response_code         INT,
    sunat_message               TEXT,
    hash_code                   VARCHAR(255),
    qr_code                     VARCHAR(255),
    xml_base64                  LONGTEXT,
    cdr_base64                  LONGTEXT,
    pdf_url                     VARCHAR(255),
    xml_url                     VARCHAR(500),
    cdr_url                     VARCHAR(500),
    created_at                  DATETIME,
    created_by                  VARCHAR(50),
    updated_at                  DATETIME,
    updated_by                  VARCHAR(50),
    deleted_at                  DATETIME,
    deleted_by                  VARCHAR(50),
    CONSTRAINT fk_rg_doc_series FOREIGN KEY (document_series_id) REFERENCES document_series (id),
    CONSTRAINT fk_rg_recipient FOREIGN KEY (recipient_id) REFERENCES recipient (id)
    );

CREATE TABLE IF NOT EXISTS remission_guide_driver
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    remission_guide_id    BIGINT NOT NULL,
    driver_doc_type       VARCHAR(5),
    driver_doc_number     VARCHAR(20),
    driver_first_name     VARCHAR(100),
    driver_last_name      VARCHAR(100),
    driver_license_number VARCHAR(30),
    vehicle_plate         VARCHAR(20),
    created_at            DATETIME,
    created_by            VARCHAR(50),
    updated_at            DATETIME,
    updated_by            VARCHAR(50),
    deleted_at            DATETIME,
    deleted_by            VARCHAR(50),
    CONSTRAINT fk_rgd_guide FOREIGN KEY (remission_guide_id) REFERENCES remission_guide (id)
    );

CREATE TABLE IF NOT EXISTS remission_guide_item
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    remission_guide_id BIGINT         NOT NULL,
    product_id         BIGINT,
    description        VARCHAR(500),
    quantity           DECIMAL(14, 3) NOT NULL DEFAULT 1.000,
    unit_measure_sunat VARCHAR(10),
    unit_price         DECIMAL(14, 2)          DEFAULT 0.00,
    subtotal_amount    DECIMAL(14, 2)          DEFAULT 0.00,
    tax_amount         DECIMAL(14, 2)          DEFAULT 0.00,
    total_amount       DECIMAL(14, 2)          DEFAULT 0.00,
    created_at         DATETIME,
    created_by         VARCHAR(50),
    updated_at         DATETIME,
    updated_by         VARCHAR(50),
    deleted_at         DATETIME,
    deleted_by         VARCHAR(50),
    CONSTRAINT fk_rgi_guide FOREIGN KEY (remission_guide_id) REFERENCES remission_guide (id),
    CONSTRAINT fk_rgi_product FOREIGN KEY (product_id) REFERENCES product (id)
    );

CREATE TABLE IF NOT EXISTS sunat_request_log
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_type    VARCHAR(30),
    document_id      BIGINT,
    request_payload  TEXT,
    response_payload TEXT,
    http_status      INT,
    success          TINYINT NOT NULL DEFAULT 0,
    error_message    TEXT,
    created_at       DATETIME         DEFAULT CURRENT_TIMESTAMP
    );

-- ============================================================
-- DATOS MAESTROS
-- ============================================================

-- document_type_sunat
INSERT IGNORE INTO document_type_sunat (code, name, status, created_at)
VALUES ('01', 'Factura', 1, NOW()),
       ('03', 'Boleta de Venta', 1, NOW()),
       ('07', 'Nota de Crédito', 1, NOW()),
       ('08', 'Nota de Débito', 1, NOW()),
       ('09', 'Guía de Remisión Remitente', 1, NOW());

-- payment_method
INSERT IGNORE INTO payment_method (code, name, requires_proof, status, created_at)
VALUES ('EFECTIVO', 'Efectivo', 0, 1, NOW()),
       ('TRANSFERENCIA', 'Transferencia Bancaria', 1, 1, NOW()),
       ('TARJETA', 'Tarjeta de Crédito/Débito', 1, 1, NOW()),
       ('YAPE_PLIN', 'Yape / Plin', 1, 1, NOW()),
       ('CHEQUE', 'Cheque', 1, 1, NOW());

-- credit_debit_note_type
INSERT IGNORE INTO credit_debit_note_type (code, name, note_category, status, created_at)
VALUES ('01', 'Anulación de la operación', 'CREDIT', 1, NOW()),
       ('02', 'Anulación por error en el RUC', 'CREDIT', 1, NOW()),
       ('07', 'Bonificación', 'CREDIT', 1, NOW()),
       ('13', 'Ajuste en operaciones de exportación', 'CREDIT', 1, NOW()),
       ('01D', 'Intereses por mora', 'DEBIT', 1, NOW()),
       ('02D', 'Gastos del cobro de la deuda', 'DEBIT', 1, NOW());

-- person_type
INSERT IGNORE INTO person_type (name, description, status, created_at)
VALUES ('Natural', 'Persona natural', 1, NOW()),
       ('Jurídica', 'Persona jurídica', 1, NOW());

-- document_type
INSERT INTO document_type (person_type_id, name, length, description, status, created_at)
SELECT id,
       'DNI',
       8,
       'Documento Nacional de Identidad',
       1,
       NOW()
FROM person_type
WHERE name = 'Natural'
    ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO document_type (person_type_id, name, length, description, status, created_at)
SELECT id,
       'RUC',
       11,
       'Registro Único de Contribuyentes',
       1,
       NOW()
FROM person_type
WHERE name = 'Jurídica'
    ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO document_type (person_type_id, name, length, description, status, created_at)
SELECT id,
       'Carnet de Extranjería',
       12,
       'Carnet de Extranjería',
       1,
       NOW()
FROM person_type
WHERE name = 'Natural'
    ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO document_type (person_type_id, name, length, description, status, created_at)
SELECT id,
       'Pasaporte',
       9,
       'Pasaporte',
       1,
       NOW()
FROM person_type
WHERE name = 'Natural'
    ON DUPLICATE KEY UPDATE name = VALUES(name);

-- unit_measure
INSERT IGNORE INTO unit_measure (code, code_sunat, name, symbol, status, created_at)
VALUES ('UND', 'NIU', 'Unidad', 'UND', 1, NOW()),
       ('KGS', 'KGM', 'Kilogramo', 'KG', 1, NOW()),
       ('MES', 'MES', 'Mes', 'MES', 1, NOW()),
       ('HR', 'HUR', 'Hora', 'HR', 1, NOW()),
       ('MT', 'MTR', 'Metro', 'MT', 1, NOW()),
       ('LT', 'LTR', 'Litro', 'LT', 1, NOW());

-- charge_unit
INSERT IGNORE INTO charge_unit (code, name, status, created_at)
VALUES ('POR-HORA', 'Por Hora', 1, NOW()),
       ('POR-PROYECTO', 'Por Proyecto', 1, NOW()),
       ('MENSUAL', 'Mensual', 1, NOW()),
       ('POR-UNIDAD', 'Por Unidad', 1, NOW());

-- sku_sequence
INSERT IGNORE INTO sku_sequence (type, last_seq_value)
VALUES ('PRD', 0),
       ('SRV', 0);

-- default categories
INSERT IGNORE INTO category (name, description, status, created_at)
VALUES ('General', 'Categoría general', 1, NOW()),
       ('Electrónica', 'Productos electrónicos', 1, NOW()),
       ('Herramientas', 'Herramientas y equipos', 1, NOW());

INSERT IGNORE INTO service_category (name, description, status, created_at)
VALUES ('General', 'Servicios generales', 1, NOW()),
       ('Mantenimiento', 'Servicios de mantenimiento', 1, NOW()),
       ('Consultoría', 'Servicios de consultoría', 1, NOW());