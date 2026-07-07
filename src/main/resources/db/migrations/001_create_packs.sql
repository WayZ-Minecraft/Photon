CREATE TABLE IF NOT EXISTS Packs (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT DEFAULT NULL,
    category VARCHAR(255),
    stripe_price_id TEXT NOT NULL UNIQUE,
    file_path TEXT DEFAULT '/downloads/{id}.zip',
    version_number TEXT DEFAULT '1.0',
    status TEXT DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
