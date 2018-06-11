package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.exceptions.ConflictException
import ru.mail.park.databases.exceptions.InvalidRelation
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.models.Post
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource
import javax.xml.crypto.Data
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

@Component
class PostDAO(private val dataSource: DataSource, private val jdbcTemplate: JdbcTemplate, private val userDAO: UserDAO) {

    private val connection: Connection = dataSource.connection

    public var postsCount: AtomicInteger = countPosts()

    @Suppress("PropertyName")
    internal val POST_ROW_MAPPER = { res: ResultSet, _: Any ->
        @Suppress("UNCHECKED_CAST")
        val children: ArrayList<Int> = try {
            ArrayList(Arrays.asList(*res.getArray("children_ids").array as Array<Int>))
        } catch (e: NullPointerException) {
            ArrayList<Int>()
        }

        Post(
                res.getInt("id"),
                res.getBoolean("is_edited"),
                res.getString("message"),
                res.getInt("parent_id"),
                children,
                res.getDate("created_at"),
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

    fun getById(id: Int): Post? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT id, slug, message, is_edited, created_at, parent_id, children_ids, author_id, forum_id, thread_id " +
                            "FROM posts " +
                            "WHERE id = ?",
                    arrayOf(id),
                    POST_ROW_MAPPER
            )
        } catch (e: DataAccessException) {
            throw NotFoundException("Post with id $id not found")
        }
    }

    fun createMultiple(posts: List<Post>): List<Post> {
        try {
            for (post in posts) {
                val authorId = userDAO.getIdByNickName(post.authorNickname!!)
                post.authorId = authorId

                if (post.parentId != 0) {
                    val parent = getById(post.parentId)

                    if (parent?.threadId != post.threadId) {
                        throw InvalidRelation("Incorrect parent for post")
                    }

                    post.childrenIds = parent?.childrenIds!!
                }
            }

            val date = Date(System.currentTimeMillis())
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            sdf.timeZone = TimeZone.getTimeZone("MSK")
            val time = sdf.format(date)

            val query = "INSERT INTO posts (message, is_edited, created_at, parent_id, children_ids, author_id, forum_id, thread_id) " +
                    "VALUES (?, ?::BOOLEAN, ?::TIMESTAMPTZ, ?, ?, ?, ?, ?)";
            val pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

            @Suppress("ConvertTryFinallyToUseCall")
            try {
                for (post in posts) {
                    val childrenArray = try {
                        connection.createArrayOf("INT4", post.childrenIds.toArray())
                    } catch (e: SQLException) {
                        connection.createArrayOf("INT4", emptyArray())
                    }

                    pst.setString(1, post.message);
                    pst.setBoolean(2, post.isEdited!!)

                    if (post.createdAt == null) {
                        pst.setString(3, time)
                    } else {
                        pst.setString(3, sdf.format(post.createdAt))
                    }

                    pst.setInt(4, post.parentId)
                    pst.setArray(5, childrenArray)
                    pst.setInt(6, post.authorId!!)
                    pst.setInt(7, post.forumId!!)
                    pst.setInt(8, post.threadId!!)

                    pst.addBatch()
                }

                pst.executeBatch()
            } finally {
                pst.close()
            }

            return posts
        } catch (e: DuplicateKeyException) {
            throw ConflictException(e.message ?: "Key duplicates on post batch creation")
        }
    }
}