package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.controllers.ForumsController
import ru.mail.park.databases.controllers.ThreadsController
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.models.Forum
import ru.mail.park.databases.models.Thread
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger

@Component
class ForumDAO(private val jdbcTemplate: JdbcTemplate, private val threadDAO: ThreadDAO, private val userDAO: UserDAO) {

    public var forumsCount: AtomicInteger = countForums()

    @Suppress("PropertyName")
    internal val FORUM_ROW_MAPPER = { res: ResultSet, _: Any ->
        Forum(
                res.getString("slug"),
                res.getString("title"),
                res.getInt("threads_count"),
                res.getInt("posts_count"),
                res.getString("author_id")
        )
    }

    private fun countForums(): AtomicInteger {
        return try {
            val forumsCount = jdbcTemplate.queryForObject("SELECT count(*) FROM forums", Int::class.java)
            AtomicInteger(forumsCount ?: 0)
        } catch (e: DataAccessException) {
            AtomicInteger(0)
        }
    }

    fun getSlugFromDBBySlug(slug: String): String? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT slug FROM forums WHERE slug = ?::citext",
                    arrayOf(slug),
                    String::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found")
        }
    }

    fun create(forumRequest: ForumsController.ForumRequest): Forum? {
        val authorNickname = userDAO.getDatabaseNicknamByNickname(forumRequest.user)
        val forum = jdbcTemplate.queryForObject(
                "INSERT INTO forums (title, slug, author_id) " +
                        "VALUES (?, ?, ?) " +
                        "RETURNING title, slug, threads_count, posts_count, author_id",
                arrayOf(forumRequest.title, forumRequest.slug, authorNickname),
                FORUM_ROW_MAPPER
        )
        forumsCount.incrementAndGet()
        return forum
    }

    fun createRelatedThread(forumSlug: String, threadRequest: ThreadsController.ThreadCreateRequest): Thread? {
        val thread = Thread(threadRequest.authorNickname, threadRequest.message, threadRequest.title)
        thread.createdAt = threadRequest.createdAt
        thread.forumSlug = getSlugFromDBBySlug(forumSlug)
        thread.forumSlug = getSlugFromDBBySlug(forumSlug)
        thread.slug = threadRequest.slug

        val created = threadDAO.create(thread)

        try {
            jdbcTemplate.queryForObject(
                    "UPDATE forums SET threads_count = threads_count + 1 WHERE slug = ?::citext RETURNING slug",
                    arrayOf(forumSlug),
                    String::class.java
            )
        } catch (e: EmptyResultDataAccessException) {

        }

        return created
    }

    fun getBySlug(slug: String): Forum? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT * FROM forums WHERE slug = ?::citext",
                    arrayOf(slug),
                    FORUM_ROW_MAPPER
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found")
        }
    }

    fun getBySlugWithCounters(slug: String): Forum? {
        return try {
            val forum = jdbcTemplate.queryForObject(
                    "SELECT * FROM forums WHERE slug = ?::citext",
                    arrayOf(slug),
                    FORUM_ROW_MAPPER
            )
            forum?.threadsCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM threads WHERE forum_id = ?",
                    arrayOf(forum?.slug),
                    Int::class.java
            )
            forum?.postsCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM posts WHERE forum_id = ?",
                    arrayOf(forum?.slug),
                    Int::class.java
            )
            forum
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found")
        }
    }

    fun getRelatedThreads(slug: String, limit: Int?, since: String?, desc: Boolean?): List<Thread>? {
        val forumSlug = getSlugFromDBBySlug(slug)
        return threadDAO.getByForumId(forumSlug!!, limit, since, desc)
    }
}
