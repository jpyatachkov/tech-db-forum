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
                res.getInt("id"),
                res.getString("slug"),
                res.getString("title"),
                res.getInt("threads_count"),
                res.getInt("posts_count"),
                res.getInt("author_id")
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

    fun getIdBySlug(slug: String): Int? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT id FROM forums WHERE slug = ?::citext",
                    arrayOf(slug),
                    Int::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found")
        }
    }

    fun getSlugById(id: Int): String? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT slug FROM forums WHERE id = ?",
                    arrayOf(id),
                    String::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with id $id not found")
        }
    }

    fun create(forumRequest: ForumsController.ForumRequest): Forum? {
        val authorId = userDAO.getIdByNickName(forumRequest.user)
        val forum = jdbcTemplate.queryForObject(
                "INSERT INTO forums (title, slug, author_id) " +
                        "VALUES (?, ?, ?) RETURNING id, title, slug, threads_count, posts_count, author_id",
                arrayOf(forumRequest.title, forumRequest.slug, authorId),
                FORUM_ROW_MAPPER
        )
        forum?.authorNickname = userDAO.getNickNameById(forum!!.authorId!!)
        forumsCount.incrementAndGet()
        return forum
    }

    fun createRelatedThread(forumSlug: String, threadRequest: ThreadsController.ThreadCreateRequest): Thread? {
        val thread = Thread(threadRequest.authorNickname, threadRequest.message, threadRequest.title)
        thread.createdAt = threadRequest.createdAt
        thread.forumId = getIdBySlug(forumSlug)
        thread.slug = threadRequest.slug

        val created = threadDAO.create(thread)
        created?.forumSlug = getSlugById(created?.forumId!!)
        return created
    }

    fun getBySlug(slug: String): Forum? {
        return try {
            val forum = jdbcTemplate.queryForObject(
                    "SELECT id, title, slug, threads_count, posts_count, author_id " +
                            "FROM forums WHERE slug = ?::citext",
                    arrayOf(slug),
                    FORUM_ROW_MAPPER
            )
            forum?.authorNickname = userDAO.getNickNameById(forum!!.authorId!!)
            forum
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found")
        }
    }

    fun getBySlugWithCounters(slug: String): Forum? {
        return try {
            val forum = jdbcTemplate.queryForObject(
                    "SELECT id, title, slug, threads_count, posts_count, author_id " +
                            "FROM forums WHERE slug = ?::citext",
                    arrayOf(slug),
                    FORUM_ROW_MAPPER
            )
            forum?.authorNickname = userDAO.getNickNameById(forum!!.authorId!!)
            forum.threadsCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM threads WHERE forum_id = ?",
                    arrayOf(forum.id),
                    Int::class.java
            )
            forum.postsCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM posts WHERE forum_id = ?",
                    arrayOf(forum.id),
                    Int::class.java
            )
            forum
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Forum with slug $slug not found")
        }
    }

    fun getRelatedThreads(slug: String, limit: Int?, since: String?, desc: Boolean?): List<Thread>? {
        val forumId = getIdBySlug(slug)
        return threadDAO.getByForumId(forumId!!, limit, since, desc)
    }
}
