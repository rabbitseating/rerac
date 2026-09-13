-- RERAC demo schema
-- Reconstructed for the portfolio version from the queries used by the original application.

CREATE TABLE IF NOT EXISTS totalrisk (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    blk8 INT NOT NULL,
    ttc8 DOUBLE NOT NULL,
    blk23 INT NOT NULL,
    ttc23 DOUBLE NOT NULL,
    blk51 INT NOT NULL,
    ttc51 DOUBLE NOT NULL,
    blk72 INT NOT NULL,
    ttc72 DOUBLE NOT NULL,
    blk73 INT NOT NULL,
    ttc73 DOUBLE NOT NULL,
    blkSIT INT NOT NULL,
    ttcSIT DOUBLE NOT NULL,
    time_val TIME NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS hml (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    location VARCHAR(32) NOT NULL,
    avgrisk DECIMAL(5,2) NOT NULL,
    hour TINYINT UNSIGNED NOT NULL,
    date DATE NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_hml_location_date_hour (location, date, hour)
);
