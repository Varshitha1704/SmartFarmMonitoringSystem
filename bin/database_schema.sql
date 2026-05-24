CREATE DATABASE IF NOT EXISTS smartfarm;
USE smartfarm;

CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(60) PRIMARY KEY,
    password VARCHAR(120) NOT NULL
);

INSERT IGNORE INTO users(username, password) VALUES ('admin', 'admin123');

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
