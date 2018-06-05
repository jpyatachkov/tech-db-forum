package ru.mail.park.databases.dao

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.ApiException
import ru.mail.park.databases.controllers.UsersController
import ru.mail.park.databases.helpers.UserDAOHelper
import ru.mail.park.databases.models.User
import java.sql.ResultSet
import java.util.concurrent.atomic.AtomicInteger

@Component
class UserDAO(private val jdbcTemplate: JdbcTemplate) {

    private var usersCount: AtomicInteger = countUsers();

    @Suppress("PropertyName")
    internal val USER_ROW_MAPPER = { res: ResultSet, _: Any ->
        User(
                Integer.parseInt(res.getString("id")),
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

    fun getByNickName(nickName: String): User? {
        try {
            val query = UserDAOHelper.GET_BY_NICKNAME_QUERY;
            return jdbcTemplate.queryForObject(query, arrayOf(nickName), USER_ROW_MAPPER);
        } catch (e: DataAccessException) {
            throw ApiException(e.message);
        }
    }

    fun getByNickNameOrEmail(nickName: String, email: String): List<User?> {
        try {
            val query = UserDAOHelper.GET_BY_NICKNAME_OR_EMAIL_QUERY;
            return jdbcTemplate.query(query, arrayOf(nickName, email), USER_ROW_MAPPER);
        } catch (e: DataAccessException) {
            throw ApiException(e.message);
        }
    }

    fun create(userRequest: UsersController.UserRequest): User? {
        try {
            val query = UserDAOHelper.CREATE_USER_QUERY;
            return jdbcTemplate.queryForObject(
                    query,
                    arrayOf(userRequest.nickname, userRequest.email, userRequest.fullname, userRequest.about),
                    USER_ROW_MAPPER
            );
        } catch (e: DataAccessException) {
            throw ApiException(e.message);
        }
    }

    fun update(userRequest: UsersController.UserRequest): User? {
        try {
            val query = UserDAOHelper.UPDATE_USER_QUERY;
            return jdbcTemplate.queryForObject(
                    query,
                    arrayOf(
                            userRequest.nickname,
                            userRequest.email,
                            userRequest.fullname,
                            userRequest.about,
                            userRequest.nickname
                    ),
                    USER_ROW_MAPPER
            );
        } catch (e: DataAccessException) {
            throw ApiException(e.message);
        }
    }
}