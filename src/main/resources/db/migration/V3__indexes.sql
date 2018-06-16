CREATE INDEX forum_id_created_at_idx
  ON threads (forum_id, created_at);

CREATE INDEX thread_id_id_idx
  ON posts (thread_id, id);
CREATE INDEX thread_id_materialized_path_idx
  ON posts (thread_id, materialized_path);
