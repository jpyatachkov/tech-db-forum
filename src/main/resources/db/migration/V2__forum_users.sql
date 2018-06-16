CREATE TABLE forum_users (
  forum_slug    CITEXT COLLATE "C" NOT NULL,
  nickname  CITEXT COLLATE "C",
  email     CITEXT UNIQUE,
  full_name TEXT NOT NULL,
  about     TEXT NOT NULL,
  UNIQUE (forum_slug, nickname)
);
