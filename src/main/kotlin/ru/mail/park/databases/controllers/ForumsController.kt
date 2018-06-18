package ru.mail.park.databases.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mail.park.databases.dao.ForumDAO
import ru.mail.park.databases.dao.ThreadDAO
import ru.mail.park.databases.dao.UserDAO
import ru.mail.park.databases.helpers.ApiHelper
import ru.mail.park.databases.models.Forum
import ru.mail.park.databases.models.Thread


@RestController
@RequestMapping(
        path = [ApiHelper.FORUM_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class ForumsController(private val forumDAO: ForumDAO,
                       private val threadDAO: ThreadDAO,
                       private val userDAO: UserDAO) {

    @PostMapping(path = ["create"])
    fun createForum(@RequestBody forumRequest: ForumRequest): ResponseEntity<*> {
        var forum: Forum?;
        return try {
            forum = forumDAO.create(forumRequest);
            ResponseEntity.status(HttpStatus.CREATED).body(forum);
        } catch (e: DuplicateKeyException) {
            forum = forumDAO.getBySlug(forumRequest.slug);
            ResponseEntity.status(HttpStatus.CONFLICT).body(forum);
        }
    }

    @PostMapping(path = ["{slug}/create"])
    fun createRelatedThread(@PathVariable slug: String,
                            @RequestBody threadRequest: ThreadsController.ThreadCreateRequest): ResponseEntity<*> {
        var thread: Thread?
        return try {
            thread = forumDAO.createRelatedThread(slug, threadRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(thread)
        } catch (e: DuplicateKeyException) {
            thread = threadDAO.getBySlugOrId(threadRequest.slug!!)
            ResponseEntity.status(HttpStatus.CONFLICT).body(thread)
        }
    }

    @GetMapping(path = ["{slug}/details"])
    fun getForumDetails(@PathVariable slug: String): ResponseEntity<*> {
        // TODO 1
        val forum = forumDAO.getBySlug(slug);
        return ResponseEntity.ok(forum!!);
    }

    @GetMapping(path = ["{slug}/threads"])
    fun getRelatedThreads(@PathVariable slug: String,
                          @RequestParam(value = "limit", required = false) limit: Int?,
                          @RequestParam(value = "since", required = false) since: String?,
                          @RequestParam(value = "desc", required = false) desc: Boolean?): ResponseEntity<*> {
        val correctSlug = forumDAO.getSlugFromDBBySlug(slug)!!
        val threads = forumDAO.getRelatedThreads(correctSlug, limit, since, desc)
        return ResponseEntity.status(HttpStatus.OK).body(threads)
    }

    @GetMapping(path = ["{slug}/users"])
    fun getRelatedUsers(@PathVariable slug: String,
                        @RequestParam(value = "limit", required = false) limit: Int?,
                        @RequestParam(value = "since", required = false) since: String?,
                        @RequestParam(value = "desc", required = false) desc: Boolean?): ResponseEntity<*> {
        val correctSlug = forumDAO.getSlugFromDBBySlug(slug)!!
        return ResponseEntity.status(HttpStatus.OK).body(userDAO.getByForumId(correctSlug, limit, since, desc))
    }

    data class ForumRequest @JsonCreator
    constructor(@param:JsonProperty(value = "slug") val slug: String,
                @param:JsonProperty(value = "title") val title: String,
                @param:JsonProperty(value = "user") val user: String);
}