CREATE DATABASE IF NOT EXISTS smartfarm;
USE smartfarm;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(60) PRIMARY KEY,
    password VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'farmer'
);

INSERT IGNORE INTO users(username, password, role) VALUES ('admin', 'admin123', 'admin');
INSERT IGNORE INTO users(username, password, role) VALUES ('farmer', 'farmer123', 'farmer');

CREATE TABLE IF NOT EXISTS farmer_profiles (
    username VARCHAR(60) PRIMARY KEY,
    full_name VARCHAR(120),
    phone VARCHAR(40),
    crop VARCHAR(80),
    farm_location VARCHAR(160),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_farmer_profile_user FOREIGN KEY (username) REFERENCES users(username)
);

INSERT IGNORE INTO farmer_profiles(username, full_name, phone, crop, farm_location)
VALUES ('farmer', 'Demo Farmer', '9999999999', 'Tomato', 'Demo Farm');

CREATE TABLE IF NOT EXISTS sensor_readings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    temperature DOUBLE,
    humidity DOUBLE,
    soil_moisture DOUBLE,
    water_level DOUBLE,
    pump_status VARCHAR(10),
    water_usage DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS disease_predictions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    image_path VARCHAR(500),
    disease_name VARCHAR(120),
    cause_text TEXT,
    pesticide TEXT,
    prevention TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
