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

@Component
class UserDAO(private val jdbcTemplate: JdbcTemplate) {

    public var usersCount: AtomicInteger = countUsers()

    @Suppress("PropertyName")
    internal val USER_ROW_MAPPER = { res: ResultSet, _: Any ->
        User(
                res.getInt("id"),
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

    fun getIdByNickName(nickName: String): Int? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE nickname = ?::citext",
                    arrayOf(nickName),
                    Int::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName not found")
        }
    }

    fun getNickNameById(id: Int): String? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT nickname FROM users WHERE id = ?",
                    arrayOf(id),
                    String::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with id $id not found")
        }
    }

    fun getByNickName(nickName: String): User? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT id, nickname, email, full_name, about FROM users WHERE nickname = ?::citext",
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
                    "SELECT id, nickname, email, full_name, about FROM users " +
                            "WHERE nickname = ?::citext OR email = ?::citext",
                    arrayOf(nickName, email),
                    USER_ROW_MAPPER
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName or email $email not found")
        }
    }

    fun getById(id: Int): User? {
        return try {
            jdbcTemplate.queryForObject(
                    "SELECT id, nickname, email, full_name, about FROM users " +
                            "WHERE id = ?",
                    arrayOf(id),
                    USER_ROW_MAPPER
            )
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with id $id not found")
        }
    }

    fun create(createRequest: UsersController.UserCreateRequest): User? {
        val user = jdbcTemplate.queryForObject(
                "INSERT INTO users (nickname, email, full_name, about) " +
                        "VALUES (?, ?, ?, ?) " +
                        "RETURNING id, nickname, email, full_name, about",
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
                            "SET nickname = coalesce(?, nickname), " +
                            "email = coalesce(?, email), " +
                            "full_name = coalesce(?, full_name), " +
                            "about = coalesce(?, about) " +
                            "WHERE nickname = ?::citext " +
                            "RETURNING id, nickname, email, full_name, about",
                    arrayOf(
                            updateRequest.nickname,
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