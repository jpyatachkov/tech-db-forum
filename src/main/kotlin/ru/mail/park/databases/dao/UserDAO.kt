package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.controllers.UsersController
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.models.User
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.crypto.Data

@Component
class UserDAO(private val jdbcTemplate: JdbcTemplate) {

    public var usersCount: AtomicInteger = countUsers()

    @Suppress("PropertyName")
    internal val USER_ROW_MAPPER = { res: ResultSet, _: Any ->
        User(
                res.getString("about"),
                res.getString("nickname"),
                res.getString("full_name"),
                res.getString("email")
        )
    }

    private fun countUsers(): AtomicInteger {
        return try {
            val usersCount = jdbcTemplate.queryForObject("SELECT count(*) FROM users", Int::class.java)
            AtomicInteger(usersCount ?: 0)
        } catch (e: DataAccessException) {
            AtomicInteger(0)
        }
    }

    fun getDatabaseNicknamByNickname(nickName: String): String? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT nickname FROM users WHERE nickname = ?::citext",
                    arrayOf(nickName),
                    String::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName not found")
        }
    }

    fun getByNickName(nickName: String): User? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT * FROM users WHERE nickname = ?::citext",
                    arrayOf(nickName),
                    USER_ROW_MAPPER
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName not found")
        }
    }

    fun getByNickNameOrEmail(nickName: String, email: String): List<User?> {
        return try {
            jdbcTemplate.query(
                    "SELECT * FROM users WHERE nickname = ?::citext OR email = ?::citext",
                    arrayOf(nickName, email),
                    USER_ROW_MAPPER
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName or email $email not found")
        }
    }

    fun getByForumId(slug: String, limit: Int?, since: String?, desc: Boolean?): List<User>? {
        return try {
            val result: List<User>?

            if (since != null) {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT nickname, email, full_name, about FROM users u " +
                                    "JOIN (SELECT user_nickname FROM forum_users WHERE forum_slug = ?::citext AND user_nickname < ?::citext) fu " +
                                    "ON u.nickname = fu.user_nickname::citext " +
                                    "ORDER BY u.nickname DESC LIMIT ?",
                            arrayOf(slug, since, limit),
                            USER_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT nickname, email, full_name, about FROM users u " +
                                    "JOIN (SELECT user_nickname FROM forum_users WHERE forum_slug = ?::citext and user_nickname > ?::citext) fu " +
                                    "ON u.nickname = fu.user_nickname::citext " +
                                    "ORDER BY u.nickname LIMIT ?",
                            arrayOf(slug, since, limit),
                            USER_ROW_MAPPER
                    )
                }
            } else {
                result = if (desc == true) {
                    jdbcTemplate.query(
                            "SELECT nickname, email, full_name, about FROM users u " +
                                    "JOIN (SELECT user_nickname FROM forum_users WHERE forum_slug = ?::citext) fu " +
                                    "ON u.nickname = fu.user_nickname::citext " +
                                    "ORDER BY u.nickname DESC LIMIT ?",
                            arrayOf(slug, limit),
                            USER_ROW_MAPPER
                    )
                } else {
                    jdbcTemplate.query(
                            "SELECT nickname, email, full_name, about FROM users u " +
                                    "JOIN (SELECT user_nickname FROM forum_users WHERE forum_slug = ?::citext) fu " +
                                    "ON u.nickname = fu.user_nickname::citext " +
                                    "ORDER BY u.nickname LIMIT ?",
                            arrayOf(slug, limit),
                            USER_ROW_MAPPER
                    )
                }
            }

            result
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("")
        }
    }

    fun create(createRequest: UsersController.UserCreateRequest): User? {
        val user = jdbcTemplate.queryForObject(
                "INSERT INTO users (nickname, email, full_name, about) " +
                        "VALUES (?, ?, ?, ?) " +
                        "RETURNING nickname, email, full_name, about",
                arrayOf(createRequest.nickname, createRequest.email, createRequest.fullname, createRequest.about),
                USER_ROW_MAPPER
        )
        usersCount.incrementAndGet()
        return user
    }

    fun update(updateRequest: UsersController.UserUpdateRequest): User? {
        return try {
            jdbcTemplate.queryForObject(
                    "UPDATE users " +
                            "SET email = coalesce(?, email), " +
                            "full_name = coalesce(?, full_name), " +
                            "about = coalesce(?, about) " +
                            "WHERE nickname = ?::citext " +
                            "RETURNING nickname, email, full_name, about",
                    arrayOf(
                            updateRequest.email,
                            updateRequest.fullname,
                            updateRequest.about,
                            updateRequest.nickname
                    ),
                    USER_ROW_MAPPER
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname ${updateRequest.nickname} not found")
        }
    }
}