package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.controllers.PostsController
import ru.mail.park.databases.exceptions.ConflictException
import ru.mail.park.databases.exceptions.InvalidRelation
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.helpers.DateTimeHelper
import ru.mail.park.databases.models.Post
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import kotlin.collections.ArrayList

@Component
class PostDAO(private val dataSource: DataSource,
              private val jdbcTemplate: JdbcTemplate,
              private val userDAO: UserDAO) {

    private val connection: Connection = dataSource.connection

    public var postsCount: AtomicInteger = countPosts()

    @Suppress("PropertyName")
    internal val POST_ROW_MAPPER = { res: ResultSet, _: Any ->
        @Suppress("UNCHECKED_CAST")
        val children: ArrayList<Int> = try {
            ArrayList(Arrays.asList(*res.getArray("materialized_path").array as Array<Int>))
        } catch (e: Exception) {
            ArrayList<Int>()
        }

        val createdAt = res.getTimestamp("created_at")

        Post(
                res.getInt("id"),
                res.getBoolean("is_edited"),
                res.getString("message"),
                res.getInt("parent_id"),
                children,
                if (createdAt != null) createdAt.toInstant().toString() else null,
                res.getInt("author_id"),
                res.getInt("thread_id"),
                res.getInt("forum_id")
        )
    }

    private fun countPosts(): AtomicInteger {
        return try {
            val count = jdbcTemplate.queryForObject("SELECT count(*) FROM posts", Int::class.java)
            AtomicInteger(count ?: 0)
        } catch (e: DataAccessException) {
            return AtomicInteger(0)
        }
    }

    private fun getFlatSort(threadId: Int, limit: Int?, since: Int?, desc: Boolean?): List<Post>? {
        return try {
            val result: List<Post>?;

            if (since != null) {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? AND id < ? ORDER BY id DESC LIMIT ?",
                            arrayOf(threadId, since, limit),
                            POST_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? AND id > ? ORDER BY id LIMIT ?",
                            arrayOf(threadId, since, limit),
                            POST_ROW_MAPPER
                    )
                }
            } else {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? ORDER BY id DESC LIMIT ?",
                            arrayOf(threadId, limit),
                            POST_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? ORDER BY id LIMIT ?",
                            arrayOf(threadId, limit),
                            POST_ROW_MAPPER
                    )
                }
            }

            result
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("No posts for thread with id $threadId")
        }
    }

    private fun getTreeSort(threadId: Int, limit: Int?, since: Int?, desc: Boolean?): List<Post>? {
        return try {
            val result: List<Post>?

            if (since != null) {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? " +
                                    "AND materialized_path < (SELECT materialized_path FROM posts WHERE id = ?) " +
                                    "ORDER BY materialized_path DESC LIMIT ?",
                            arrayOf(threadId, since, limit),
                            POST_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? " +
                                    "AND materialized_path > (SELECT materialized_path FROM posts WHERE id = ?) " +
                                    "ORDER BY materialized_path LIMIT ?",
                            arrayOf(threadId, since, limit),
                            POST_ROW_MAPPER
                    )
                }
            } else {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? ORDER BY materialized_path DESC LIMIT ?",
                            arrayOf(threadId, limit),
                            POST_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT * FROM posts WHERE thread_id = ? ORDER BY materialized_path LIMIT ?",
                            arrayOf(threadId, limit),
                            POST_ROW_MAPPER
                    )
                }
            }

            result
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("No posts for thread with id $threadId")
        }
    }

    private fun getParentTreeSort(threadId: Int, limit: Int?, since: Int?, desc: Boolean?): List<Post>? {
        return try {
            val result: List<Post>?

            if (since != null) {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT * from posts post JOIN " +
                                    "(SELECT id FROM posts WHERE parent_id = 0 AND thread_id = ? " +
                                    "AND materialized_path[1] < (SELECT materialized_path[1] FROM posts WHERE id = ?) " +
                                    "ORDER BY id DESC LIMIT ?) root ON post.materialized_path[1] = root.id " +
                                    "ORDER BY root.id DESC, post.materialized_path",
                            arrayOf(threadId, since, limit),
                            POST_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT * from posts post JOIN " +
                                    "(SELECT id FROM posts WHERE  parent_id = 0 AND thread_id = ? " +
                                    "AND materialized_path[1] > (SELECT materialized_path[1] FROM posts WHERE id = ?) " +
                                    "ORDER BY id LIMIT ?) root ON post.materialized_path[1] = root.id " +
                                    "ORDER BY root.id, post.materialized_path",
                            arrayOf(threadId, since, limit),
                            POST_ROW_MAPPER
                    )
                }
            } else {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT * from posts post JOIN " +
                                    "(SELECT id FROM posts WHERE thread_id = ? AND parent_id = 0 ORDER BY id " +
                                    "DESC LIMIT ?) root ON post.materialized_path[1] = root.id " +
                                    "ORDER BY root.id DESC, post.materialized_path",
                            arrayOf(threadId, limit),
                            POST_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT * from posts post JOIN " +
                                    "(SELECT id FROM posts WHERE thread_id = ? AND parent_id = 0 ORDER BY id " +
                                    "LIMIT ?) root ON post.materialized_path[1] = root.id " +
                                    "ORDER BY root.id, post.materialized_path",
                            arrayOf(threadId, limit),
                            POST_ROW_MAPPER
                    )
                }
            }

            result
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("No posts for thread with id $threadId")
        }
    }

    fun get(threadId: Int, limit: Int?, since: Int?, sortOrder: String?, desc: Boolean?): List<Post>? {
        return when (sortOrder) {
            "tree" -> getTreeSort(threadId, limit, since, desc)
            "parent_tree" -> getParentTreeSort(threadId, limit, since, desc)
            else -> getFlatSort(threadId, limit, since, desc)
        }
    }

    fun getById(id: Int): Post? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT id, message, is_edited, created_at, parent_id, materialized_path, author_id, forum_id, thread_id " +
                            "FROM posts " +
                            "WHERE id = ?",
                    arrayOf(id),
                    POST_ROW_MAPPER
            )
        } catch (e: DataAccessException) {
            throw NotFoundException("Post with id $id not found")
        }
    }

    fun createMultiple(posts: List<Post>, forumSlug: String?): List<Post> {
        try {
            for (post in posts) {
                post.authorId = userDAO.getIdByNickName(post.authorNickname!!)

                if (post.parentId != 0) {
                    val parent: Post?;

                    try {
                        parent = getById(post.parentId)
                    } catch (e: NotFoundException) {
                        throw ConflictException("Вот тут нелогично ору как")
                    }

                    if (parent?.threadId != post.threadId) {
                        throw InvalidRelation("Incorrect parent for post")
                    }
                }
            }

            val query = "INSERT INTO posts (id, message, is_edited, created_at, parent_id, author_id, forum_id, thread_id, materialized_path) " +
                    "VALUES (?, ?, ?::BOOLEAN, ?::TIMESTAMPTZ, ?, ?, ?, ?, array_append((SELECT materialized_path FROM posts WHERE id = ?), ?::BIGINT))";
            val pst = connection.prepareStatement(query, Statement.NO_GENERATED_KEYS)

            val createdAt = DateTimeHelper.toISODate()

            @Suppress("ConvertTryFinallyToUseCall")
            try {
                for (post in posts) {
                    val postId = jdbcTemplate.queryForObject(
                            "SELECT nextval(pg_get_serial_sequence('posts', 'id'))",
                            Int::class.java
                    )

                    post.id = postId

                    pst.setInt(1, postId!!)
                    pst.setString(2, post.message);
                    pst.setBoolean(3, post.isEdited)

                    if (post.createdAt == null) {
                        pst.setString(4, createdAt)
                        post.createdAt = createdAt
                    } else {
                        pst.setString(4, post.createdAt)
                    }

                    pst.setInt(5, post.parentId)
                    pst.setInt(6, post.authorId!!)
                    pst.setInt(7, post.forumId!!)
                    pst.setInt(8, post.threadId!!)
                    pst.setInt(9, post.parentId)
                    pst.setInt(10, postId)

                    pst.addBatch()
                }

                pst.executeBatch()

                for (post in posts) {
                    try {
                        jdbcTemplate.queryForObject(
                                "INSERT INTO forum_users(forum_slug, user_nickname) VALUES (?, ?) RETURNING forum_slug",
                                arrayOf(forumSlug, post.authorNickname),
                                String::class.java
                        )
                    } catch (ignore: DuplicateKeyException) {

                    }

                    postsCount.incrementAndGet()
                }
            } finally {
                pst.close()
            }

            return posts
        } catch (e: DuplicateKeyException) {
            throw ConflictException(e.message ?: "Key duplicates on post batch creation")
        }
    }

    fun update(postUpdateRequest: PostsController.PostUpdateRequest): Post? {
        return try {
            val post = if (postUpdateRequest.message != null) {
                val oldPost = getById(postUpdateRequest.id)
                if (oldPost?.message == postUpdateRequest.message) {
                    oldPost
                } else {
                    jdbcTemplate.queryForObject(
                            "UPDATE posts SET message = ?, is_edited = true WHERE id = ? " +
                                    "RETURNING id, message, is_edited, created_at, parent_id, materialized_path, author_id, forum_id, thread_id",
                            arrayOf(postUpdateRequest.message, postUpdateRequest.id),
                            POST_ROW_MAPPER
                    )
                }
            } else {
                jdbcTemplate.queryForObject(
                        "UPDATE posts SET message = coalesce(?, message) WHERE id = ? " +
                                "RETURNING id, message, is_edited, created_at, parent_id, materialized_path, author_id, forum_id, thread_id",
                        arrayOf(postUpdateRequest.message, postUpdateRequest.id),
                        POST_ROW_MAPPER
                )
            }
            post?.authorNickname = userDAO.getNickNameById(post?.authorId!!)
            post
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("Post with id ${postUpdateRequest.id} not found")
        }
    }
}