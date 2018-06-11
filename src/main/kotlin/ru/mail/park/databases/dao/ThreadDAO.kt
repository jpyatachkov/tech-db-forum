package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.controllers.PostsController
import ru.mail.park.databases.controllers.ThreadsController
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.helpers.DateTimeHelper
import ru.mail.park.databases.models.Post
import ru.mail.park.databases.models.Thread
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

@Component
class ThreadDAO(private val jdbcTemplate: JdbcTemplate,
                private val postDAO: PostDAO,
                private val userDAO: UserDAO) {

    public var threadsCount: AtomicInteger = countThreads();

    @Suppress("PropertyName")
    internal val THREAD_ROW_MAPPER = { res: ResultSet, _: Any ->
        val createdAt = res.getTimestamp("created_at")

        ru.mail.park.databases.models.Thread(
                res.getInt("id"),
                res.getInt("author_id"),
                res.getString("message"),
                res.getString("title"),
                if (createdAt != null) createdAt.toInstant().toString() else null,
                res.getString("slug"),
                res.getInt("votes"),
                res.getInt("forum_id")
        )
    }

    private fun countThreads(): AtomicInteger {
        return try {
            val threadsCount = jdbcTemplate.queryForObject("SELECT count(*) FROM threads", Int::class.java)
            AtomicInteger(threadsCount ?: 0)
        } catch (e: DataAccessException) {
            AtomicInteger(0);
        }
    }

    fun getById(id: Int): Thread? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT id, title, slug, message, votes, created_at, forum_id, author_id " +
                            "FROM threads " +
                            "WHERE id = ?",
                    arrayOf(id),
                    THREAD_ROW_MAPPER
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Thread with id $id not found")
        }
    }

    fun getBySlugOrId(slugOrId: String): Thread? {
        return try {
            try {
                jdbcTemplate.queryForObject(
                        "SELECT id, title, slug, message, votes, created_at, forum_id, author_id " +
                                "FROM threads " +
                                "WHERE id = ?",
                        arrayOf(Integer.parseInt(slugOrId)),
                        THREAD_ROW_MAPPER
                )
            } catch (e: NumberFormatException) {
                jdbcTemplate.queryForObject(
                        "SELECT id, title, slug, message, votes, created_at, forum_id, author_id " +
                                "FROM threads " +
                                "WHERE slug = ?::citext",
                        arrayOf(slugOrId),
                        THREAD_ROW_MAPPER
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Thread with id or slug $slugOrId not found")
        }
    }

    fun getIdBySlugOrId(slugOrId: String): Int? {
        return try {
            try {
                jdbcTemplate.queryForObject(
                        "SELECT id " +
                                "FROM threads " +
                                "WHERE id = ?",
                        arrayOf(Integer.parseInt(slugOrId)),
                        Int::class.java
                )
            } catch (e: NumberFormatException) {
                jdbcTemplate.queryForObject(
                        "SELECT id " +
                                "FROM threads " +
                                "WHERE slug = ?::citext",
                        arrayOf(slugOrId),
                        Int::class.java
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Thread with id or slug $slugOrId not found")
        }
    }

    fun getByForumId(forumId: Int, limit: Int?, since: String?, desc: Boolean?): List<Thread>? {
        return try {
            val order: String
            val sign: String

            if (desc == true) {
                order = " DESC "
                sign = " <= "
            } else {
                order = " ASC "
                sign = " >= "
            }

            if (since != null) {
                jdbcTemplate.query(
                        "SELECT id, title, slug, message, votes, created_at, forum_id, author_id " +
                                "FROM threads " +
                                "WHERE forum_id = ? AND created_at" + sign + "?::TIMESTAMPTZ " +
                                "ORDER BY created_at " + order + " LIMIT ?",
                        arrayOf(forumId, since, limit),
                        THREAD_ROW_MAPPER
                )
            } else {
                jdbcTemplate.query(
                        "SELECT id, title, slug, message, votes, created_at, forum_id, author_id " +
                                "FROM threads " +
                                "WHERE forum_id = ? " +
                                "ORDER BY created_at " + order + " LIMIT ?",
                        arrayOf(forumId, limit),
                        THREAD_ROW_MAPPER
                )
            }
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Threads from forum with id $forumId not found")
        }
    }

    fun create(thread: Thread): Thread? {
        thread.authorId = userDAO.getIdByNickName(thread.authorNickname!!)

        if (thread.createdAt == null) {
            thread.createdAt = DateTimeHelper.toISODate()
        }

        if (thread.votesCount == null) {
            thread.votesCount = 0
        }

        val created = jdbcTemplate.queryForObject(
                "INSERT INTO threads (title, slug, message, votes, created_at, forum_id, author_id) " +
                        "VALUES (?, ?, ?, ?, ?::TIMESTAMPTZ, ?, ?) " +
                        "RETURNING id, title, slug, message, votes, created_at, forum_id, author_id",
                arrayOf(
                        thread.title,
                        thread.slug,
                        thread.message,
                        thread.votesCount,
                        thread.createdAt,
                        thread.forumId,
                        thread.authorId
                ),
                THREAD_ROW_MAPPER
        )

        created?.authorNickname = userDAO.getNickNameById(created?.authorId!!)
        threadsCount.incrementAndGet()
        return created
    }

    fun createRelatedPosts(forumSlug: String, thread: Thread, postsCreateRequest: List<PostsController.PostCreateRequest>): List<Post> {
        val posts = ArrayList<Post>()

        for (postRequest in postsCreateRequest) {
            val post = Post(
                    postRequest.authorNickname,
                    postRequest.message,
                    postRequest.parentId ?: 0,
                    postRequest.createdAt
            )
            post.forumId = thread.forumId
            post.threadId = thread.id
            post.forumSlug = forumSlug

            posts.add(post)
        }

        return postDAO.createMultiple(posts)
    }

    fun update(updateRequest: ThreadsController.ThreadUpdateRequest): Thread? {
        return try {
            val created = try {
                jdbcTemplate.queryForObject(
                        "UPDATE threads " +
                                "SET message = ?, " +
                                "title = ? " +
                                "WHERE id = ? " +
                                "RETURNING id, title, slug, message, votes, created_at, forum_id, author_id",
                        arrayOf(
                                updateRequest.message,
                                updateRequest.title,
                                Integer.parseInt(updateRequest.slugOrId)
                        ),
                        THREAD_ROW_MAPPER
                )
            } catch (e: NumberFormatException) {
                jdbcTemplate.queryForObject(
                        "UPDATE threads " +
                                "SET message = ?, " +
                                "title = ? " +
                                "WHERE slug = ?::citext " +
                                "RETURNING id, title, slug, message, votes, created_at, forum_id, author_id",
                        arrayOf(
                                updateRequest.message,
                                updateRequest.title,
                                updateRequest.slugOrId
                        ),
                        THREAD_ROW_MAPPER
                )
            }

            created?.authorNickname = userDAO.getNickNameById(created?.authorId!!)
            created
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Thread with slug or id ${updateRequest.slugOrId} not found")
        }
    }
}