CREATE TABLE IF NOT EXISTS PackOwnership (
    user_email VARCHAR(50) NOT NULL,
    account_uuid TEXT DEFAULT NULL,
    pack_id TEXT NOT NULL,
    purchased_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    first_download_at DATETIME DEFAULT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    claimed_successfully BOOLEAN DEFAULT FALSE,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_email, pack_id),
    FOREIGN KEY (pack_id) REFERENCES Packs(id) ON DELETE CASCADE
);
