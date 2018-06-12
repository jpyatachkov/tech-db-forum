CREATE INDEX forums_author_id_idx ON forums (author_id);

CREATE INDEX threads_forum_id_idx ON threads (forum_id);
CREATE INDEX threads_author_id_idx ON threads (author_id);
CREATE INDEX threads_forum_id_created_at_idx ON threads (forum_id, created_at);

CREATE INDEX posts_thread_id_idx ON posts (thread_id, id);
CREATE INDEX posts_thread_id_parent_id_idx ON posts (thread_id, parent_id);
CREATE INDEX posts_thread_id_materialized_path_idx ON posts (thread_id, materialized_path);
CREATE INDEX posts_thread_id_parent_materialized_path_idx ON posts (thread_id, (materialized_path[1]));

CREATE INDEX votes_cover_idx ON votes (thread_id, voice, user_id);

CREATE INDEX forum_users_cover_id ON forum_users (forum_slug, user_nickname);
CREATE INDEX forum_users_cover_id_reverse ON forum_users (user_nickname, forum_slug);
