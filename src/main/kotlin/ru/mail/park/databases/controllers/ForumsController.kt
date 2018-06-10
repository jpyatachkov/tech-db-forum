package ru.mail.park.databases.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.dao.DataAccessException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mail.park.databases.exceptions.ApiException
import ru.mail.park.databases.dao.ForumDAO
import ru.mail.park.databases.dao.UserDAO
import ru.mail.park.databases.helpers.ApiHelper
import ru.mail.park.databases.models.Forum


@RestController
@RequestMapping(
        path = [ApiHelper.FORUM_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class ForumsController(private val forumDAO: ForumDAO, private val userDAO: UserDAO) {

    @PostMapping(path = ["create"])
    fun createForum(@RequestBody forumRequest: ForumRequest): ResponseEntity<*> {
        var forum: Forum?;
        return try {
            forum = forumDAO.create(forumRequest);
            ResponseEntity.status(HttpStatus.CREATED).body(forum);
        } catch (e: DuplicateKeyException) {
            forum = forumDAO.getBySlug(forumRequest.slug);
            forum?.authorNickname = userDAO.getNickNameById(forum!!.authorId!!);
            ResponseEntity.status(HttpStatus.CONFLICT).body(forum);
        }
    }

    fun createRelatedThread() {

    }

    @GetMapping(path = ["{slug}/details"])
    fun getForumDetails(@PathVariable slug: String): ResponseEntity<*> {
        val forum = forumDAO.getBySlug(slug);
        return ResponseEntity.ok(forum!!);
    }

    fun getRelatedThreads() {

    }

    fun getRelatedUsers() {

    }

    data class ForumRequest @JsonCreator
    constructor(@param:JsonProperty(value = "slug") val slug: String,
                @param:JsonProperty(value = "title") val title: String,
                @param:JsonProperty(value = "user") val user: String);
}