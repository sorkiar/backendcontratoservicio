CREATE TABLE IF NOT EXISTS ubigeo
(
    ubigeo     VARCHAR(8)        NOT NULL PRIMARY KEY,
    department VARCHAR(50)       NULL,
    province   VARCHAR(50)       NULL,
    distrit    VARCHAR(50)       NULL,
    status     TINYINT DEFAULT 1 NOT NULL
);
