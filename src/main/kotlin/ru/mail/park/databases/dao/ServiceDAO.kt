package ru.mail.park.databases.dao

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger

@Component
class ServiceDAO(private val jdbcTemplate: JdbcTemplate,
                 private val forumDAO: ForumDAO,
                 private val postDAO: PostDAO,
                 private val threadDAO: ThreadDAO,
                 private val userDAO: UserDAO) {

    fun doClear() {
        jdbcTemplate.update("TRUNCATE TABLE forums, posts, threads, users, votes, forum_users")

        forumDAO.forumsCount = AtomicInteger(0)
        postDAO.postsCount = AtomicInteger(0)
        threadDAO.threadsCount = AtomicInteger(0)
        userDAO.usersCount = AtomicInteger(0)
    }

    fun getStatus(): ServiceDetails {
        return ServiceDetails(
                forumDAO.forumsCount.toInt(),
                postDAO.postsCount.toInt(),
                threadDAO.threadsCount.toInt(),
                userDAO.usersCount.toInt()
        )
    }

    class ServiceDetails(forumsCount: Int,
                         postsCount: Int,
                         threadsCount: Int,
                         usersCount: Int) {

        @get:JsonProperty(value = "forum")
        public val forumsCount: Int = forumsCount

        @get:JsonProperty(value = "post")
        public val postsCount: Int = postsCount

        @get:JsonProperty(value = "thread")
        public val threadsCount: Int = threadsCount

        @get:JsonProperty(value = "user")
        public val usersCount: Int = usersCount
    }
}