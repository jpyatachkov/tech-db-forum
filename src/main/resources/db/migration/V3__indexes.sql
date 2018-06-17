CREATE INDEX forum_id_created_at_idx
  ON threads (forum_id, created_at);

CREATE INDEX thread_id_id_idx
  ON posts (thread_id, id);
CREATE INDEX thread_id_materialized_path_idx
  ON posts (thread_id, materialized_path);

CREATE INDEX post_thread_id_id_idx
  ON posts (thread_id, id);
CREATE INDEX post_thread_id_materialized_path_idx
  ON posts (thread_id, materialized_path);
CREATE INDEX post_thread_id_parent_id_root_id_id_idx
  ON posts (thread_id, parent_id, root_id, id);
CREATE INDEX post_root_id_id_materialized_path_idx
  ON posts (root_id, id, materialized_path);
CREATE INDEX post_root_id_materialized_path_desc_id_idx
  ON posts (root_id, materialized_path DESC, id);
