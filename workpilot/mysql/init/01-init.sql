-- Initialize workpilot database
-- This script runs when the MySQL container starts for the first time

-- Create database if it doesn't exist (should already be created by environment variables)
CREATE DATABASE IF NOT EXISTS workpilot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE workpilot;

-- Set proper character set and collation
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET character_set_connection=utf8mb4;

-- Create additional users if needed (optional)
-- CREATE USER IF NOT EXISTS 'workpilot_app'@'%' IDENTIFIED BY 'workpilot_app_password';
-- GRANT ALL PRIVILEGES ON workpilot.* TO 'workpilot_app'@'%';

-- Flush privileges
FLUSH PRIVILEGES;

-- Add any initial data or schema modifications here if needed
-- For example, you might want to insert some default data

-- Example: Insert default roles or permissions if needed
-- INSERT IGNORE INTO roles (name, description) VALUES 
-- ('ADMIN', 'Administrator role'),
-- ('USER', 'Regular user role');

-- Example: Insert default configuration if needed
-- INSERT IGNORE INTO app_config (config_key, config_value) VALUES 
-- ('app.version', '1.0.0'),
-- ('app.environment', 'docker'); 