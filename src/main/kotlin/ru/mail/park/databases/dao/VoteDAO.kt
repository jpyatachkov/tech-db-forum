package ru.mail.park.databases.dao

import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import ru.mail.park.databases.exceptions.NotFoundException
import ru.mail.park.databases.models.User
import java.lang.Math.abs
import java.sql.Connection
import java.sql.ResultSet
import javax.sql.DataSource

@Component
class VoteDAO(private val jdbcTemplate: JdbcTemplate) {

    fun voteForThread(threadId: Int, userNickname: String, voice: Int) {
        val oldVoice = try {
            jdbcTemplate.queryForObject(
                    "SELECT voice FROM votes WHERE thread_id = ? AND user_id = ?",
                    arrayOf(threadId, userNickname),
                    Int::class.java
            )
        } catch (e: EmptyResultDataAccessException) {
            0
        }

        if (oldVoice == 0) {
            try {
                jdbcTemplate.queryForObject(
                        "INSERT INTO votes (thread_id, voice, user_id) VALUES (?, ?, ?) RETURNING voice",
                        arrayOf(threadId, voice, userNickname),
                        Int::class.java
                )
            } catch (e: DataIntegrityViolationException) {
                throw NotFoundException("User with nickname $userNickname not found")
            }
        } else {
            jdbcTemplate.queryForObject(
                    "UPDATE votes SET voice = ? WHERE thread_id = ? AND user_id = ? RETURNING voice",
                    arrayOf(voice, threadId, userNickname),
                    Int::class.java
            )
        }

        jdbcTemplate.queryForObject(
                "UPDATE threads SET votes = votes + (?) WHERE id = ? RETURNING id",
                arrayOf(voice - oldVoice, threadId),
                Int::class.java
        )
    }
}