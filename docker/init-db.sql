-- ChitChat Database Initialization Script
-- This script is run when the PostgreSQL container is first created

-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS chitchatdb;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create indexes for better performance (will be created by JPA, but listed here for reference)
-- Index on users table for email and username lookups
-- Index on messages table for room_id and sent_at for message history
-- Index on room_participants table for user_id and room_id for room membership

-- Insert sample data (optional for development)
-- Note: This will be handled by the application itself
