CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
  nickname  CITEXT COLLATE "C" PRIMARY KEY,
  email     CITEXT UNIQUE,
  full_name TEXT NOT NULL,
  about     TEXT NOT NULL
);

CREATE TABLE forums (
  slug          CITEXT COLLATE "C" PRIMARY KEY,
  title         TEXT   NOT NULL,
  threads_count INT    NOT NULL DEFAULT 0,
  posts_count   INT    NOT NULL DEFAULT 0,
  author_id     CITEXT NOT NULL REFERENCES users (nickname)
);

CREATE TABLE threads (
  id         SERIAL PRIMARY KEY,
  slug       CITEXT COLLATE "C" UNIQUE,
  title      TEXT   NOT NULL,
  message    TEXT   NOT NULL,
  votes      INT            DEFAULT 0,
  created_at TIMESTAMPTZ(3) DEFAULT now(),
  forum_id   CITEXT NOT NULL REFERENCES forums (slug),
  author_id  CITEXT NOT NULL REFERENCES users (nickname)
);

CREATE TABLE posts (
  id                SERIAL PRIMARY KEY,
  message           TEXT    NOT NULL,
  is_edited         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at        TIMESTAMPTZ,
  root_id           INT     NOT NULL DEFAULT 0,
  parent_id         INT              DEFAULT 0,
  materialized_path INT [],
  author_id         CITEXT  NOT NULL REFERENCES users (nickname),
  forum_id          CITEXT  NOT NULL REFERENCES forums (slug),
  thread_id         INT     NOT NULL REFERENCES threads (id)
);

CREATE TABLE votes (
  thread_id INT    NOT NULL REFERENCES threads (id),
  voice     INT    NOT NULL,
  user_id   CITEXT NOT NULL REFERENCES users (nickname),
  UNIQUE (thread_id, user_id)
);
