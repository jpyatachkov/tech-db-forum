package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.controllers.UsersController
import ru.mail.park.databases.exceptions.ApiException
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.helpers.UserDAOHelper
import ru.mail.park.databases.models.User
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger

@Component
class UserDAO(private val jdbcTemplate: JdbcTemplate) {

    public var usersCount: AtomicInteger = countUsers();

    @Suppress("PropertyName")
    internal val USER_ROW_MAPPER = { res: ResultSet, _: Any ->
        User(
                res.getInt("id"),
                res.getString("full_name"),
                res.getString("email"),
                res.getString("nickname"),
                res.getString("about")
        )
    }

    private fun countUsers(): AtomicInteger {
        return try {
            val usersCount = jdbcTemplate.queryForObject(UserDAOHelper.COUNT_USERS_QUERY, Int::class.java);
            AtomicInteger(usersCount ?: 0);
        } catch (e: DataAccessException) {
            AtomicInteger(0);
        }
    }

    fun getIdByNickName(nickName: String): Int? {
        return try {
            val query = UserDAOHelper.GET_ID_BY_NICKNAME_QUERY;
            jdbcTemplate.queryForObject(query, arrayOf(nickName), Int::class.java);
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName not found");
        }
    }

    fun getNickNameById(id: Int): String? {
        return try {
            val query = UserDAOHelper.GET_NICKNAME_BY_ID_QUERY;
            jdbcTemplate.queryForObject(query, arrayOf(id), String::class.java);
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with id $id not found");
        }
    }

    fun getByNickName(nickName: String): User? {
        return try {
            val query = UserDAOHelper.GET_BY_NICKNAME_QUERY;
            jdbcTemplate.queryForObject(query, arrayOf(nickName), USER_ROW_MAPPER)
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName not found");
        }
    }

    fun getByNickNameOrEmail(nickName: String, email: String): List<User?> {
        return try {
            val query = UserDAOHelper.GET_BY_NICKNAME_OR_EMAIL_QUERY;
            jdbcTemplate.query(query, arrayOf(nickName, email), USER_ROW_MAPPER);
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname $nickName or email $email not found");
        }
    }

    fun create(createRequest: UsersController.UserCreateRequest): User? {
        val query = UserDAOHelper.CREATE_USER_QUERY;
        val user = jdbcTemplate.queryForObject(
                query,
                arrayOf(createRequest.nickname, createRequest.email, createRequest.fullname, createRequest.about),
                USER_ROW_MAPPER
        );
        usersCount.incrementAndGet();
        return user;
    }

    fun update(updateRequest: UsersController.UserUpdateRequest): User? {
        return try {
            val query = UserDAOHelper.UPDATE_USER_QUERY;
            jdbcTemplate.queryForObject(
                    query,
                    arrayOf(
                            updateRequest.nickname,
                            updateRequest.email,
                            updateRequest.fullname,
                            updateRequest.about,
                            updateRequest.nickname
                    ),
                    USER_ROW_MAPPER
            );
        } catch (e: EmptyResultDataAccessException) {
            throw NotFoundException("User with nickname ${updateRequest.nickname} not found");
        }
    }
}