-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    nickname VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(255) NOT NULL
);

-- Create news table
CREATE TABLE news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    text TEXT NOT NULL,
    image_url VARCHAR(255),
    creation_date TIMESTAMP NOT NULL,
    author_id BIGINT NOT NULL,
    CONSTRAINT fk_news_author FOREIGN KEY (author_id) REFERENCES users(id)
);

-- Create comments table
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    author_nickname VARCHAR(255) NOT NULL,
    news_id BIGINT NOT NULL,
    parent_comment_id BIGINT,
    CONSTRAINT fk_comment_news FOREIGN KEY (news_id) REFERENCES news(id),
    CONSTRAINT fk_comment_parent FOREIGN KEY (parent_comment_id) REFERENCES comments(id)
);

-- Create indexes for foreign keys to improve query performance
CREATE INDEX idx_news_author ON news(author_id);
CREATE INDEX idx_comment_news ON comments(news_id);
CREATE INDEX idx_comment_parent ON comments(parent_comment_id);