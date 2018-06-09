package ru.mail.park.databases.helpers

object ForumDAOHelper {

    const val COUNT_FORUMS_QUERY = "" +
            "SELECT count(*) " +
            "FROM forums";

    const val GET_FORUM_ID_BY_SLUG_QUERY = "" +
            "SELECT id " +
            "FROM forums " +
            "WHERE slug = ?::citext";

    const val GET_FORUM_BY_SLUG_QUERY = "" +
            "SELECT id, title, slug, threads_count, posts_count, author_id " +
            "FROM forums " +
            "WHERE slug = ?::citext";

    const val CREATE_FORUM_QUERY = "" +
            "INSERT INTO forums (title, slug, author_id) " +
            "VALUES(?, ?, ?) " +
            "RETURNING id, title, slug, threads_count, posts_count, author_id";
}