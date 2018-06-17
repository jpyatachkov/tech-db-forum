CREATE INDEX users_nickname_email
  ON users (nickname, email);

CREATE INDEX forum_id_created_at_idx
  ON threads (forum_id, created_at);

CREATE INDEX post_thread_id_id_idx
  ON posts (thread_id, id);

CREATE INDEX post_thread_id_materialized_path_idx
  ON posts (thread_id, materialized_path);
CREATE INDEX post_id_materialized_path_idx
  ON posts (id, materialized_path);

CREATE INDEX parent_id_thread_id_root_id_id_idx
  ON posts (parent_id, thread_id, root_id, id);
CREATE INDEX id_root_id_idx
  ON posts (id, root_id);
CREATE INDEX id_root_id_materialized_path_idx
  ON posts (id, root_id, materialized_path);
CREATE INDEX id_root_id_materialized_path_desc_idx
  ON posts (id, root_id, materialized_path DESC);

CREATE INDEX votes_user_id
  ON votes (user_id);
CREATE INDEX votes_thread_id_voice
  ON votes (thread_id, voice);

CREATE INDEX forum_users_nickname
  ON forum_users (user_nickname);
