CREATE TABLE forum_users (
  forum_slug    CITEXT COLLATE "C" NOT NULL,
  user_nickname CITEXT COLLATE "C" NOT NULL,
  UNIQUE (forum_slug, user_nickname)
);
