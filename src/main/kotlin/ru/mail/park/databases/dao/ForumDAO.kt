package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.exceptions.ApiException
import ru.mail.park.databases.controllers.ForumsController
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.helpers.ForumDAOHelper
import ru.mail.park.databases.models.Forum
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger

@Component
class ForumDAO(private val userDAO: UserDAO, private val jdbcTemplate: JdbcTemplate) {

    private val forumsCount: AtomicInteger = countForums();

    @Suppress("PropertyName")
    internal val FORUM_ROW_MAPPER = { res: ResultSet, _: Any ->
        Forum(
                res.getInt("id"),
                res.getString("title"),
                res.getString("slug"),
                res.getInt("threads_count"),
                res.getInt("posts_count"),
                res.getInt("author_id")
        );
    }

    private fun countForums(): AtomicInteger {
        return try {
            val forumsCount = jdbcTemplate.queryForObject(ForumDAOHelper.COUNT_FORUMS_QUERY, Int::class.java);
            AtomicInteger(forumsCount ?: 0);
        } catch (e: DataAccessException) {
            AtomicInteger(0);
        }
    }

    fun getIdBySlug(slug: String): Int? {
        return try {
            val query = ForumDAOHelper.GET_FORUM_ID_BY_SLUG_QUERY;
            jdbcTemplate.queryForObject(query, arrayOf(slug), Int::class.java);
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found");
        }
    }

    fun create(forumRequest: ForumsController.ForumRequest): Forum? {
        val authorId = userDAO.getIdByNickName(forumRequest.user);
        val query = ForumDAOHelper.CREATE_FORUM_QUERY;
        val forum = jdbcTemplate.queryForObject(
                query,
                arrayOf(forumRequest.title, forumRequest.slug, authorId),
                FORUM_ROW_MAPPER
        );

        forum?.authorNickname = userDAO.getNickNameById(forum!!.authorId);
        return forum;
    }

    fun getBySlug(slug: String): Forum? {
        return try {
            val query = ForumDAOHelper.GET_FORUM_BY_SLUG_QUERY;
            val forum = jdbcTemplate.queryForObject(query, arrayOf(slug), FORUM_ROW_MAPPER);

            forum?.authorNickname = userDAO.getNickNameById(forum!!.authorId);
            forum;
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found");
        }
    }
}