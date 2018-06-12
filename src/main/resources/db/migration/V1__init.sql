CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users (
  id        BIGSERIAL PRIMARY KEY,
  nickname  CITEXT COLLATE "C" UNIQUE,
  email     CITEXT UNIQUE,
  full_name TEXT NOT NULL,
  about     TEXT NOT NULL
);

CREATE TABLE forums (
  id            BIGSERIAL PRIMARY KEY,
  title         TEXT          NOT NULL,
  slug          CITEXT UNIQUE NOT NULL,
  threads_count BIGINT        NOT NULL DEFAULT 0,
  posts_count   BIGINT        NOT NULL DEFAULT 0,
  author_id     BIGINT        NOT NULL REFERENCES users (id)
);

CREATE TABLE threads (
  id         BIGSERIAL PRIMARY KEY,
  title      TEXT   NOT NULL,
  slug       CITEXT UNIQUE,
  message    TEXT   NOT NULL,
  votes      INTEGER        DEFAULT 0,
  created_at TIMESTAMPTZ(3) DEFAULT now(),
  forum_id   BIGINT NOT NULL REFERENCES forums (id),
  author_id  BIGINT NOT NULL REFERENCES users (id)
);

CREATE TABLE posts (
  id                BIGSERIAL PRIMARY KEY,
  message           TEXT    NOT NULL,
  is_edited         BOOLEAN NOT NULL DEFAULT FALSE,
  created_at        TIMESTAMPTZ,
  parent_id         BIGINT           DEFAULT 0,
  materialized_path BIGINT [],
  author_id         BIGINT  NOT NULL,
  forum_id          BIGINT  NOT NULL,
  thread_id         BIGINT  NOT NULL
);

CREATE TABLE votes (
  thread_id BIGINT NOT NULL REFERENCES threads (id),
  voice     INT    NOT NULL,
  user_id   BIGINT NOT NULL REFERENCES users (id),
  UNIQUE (thread_id, user_id)
);
