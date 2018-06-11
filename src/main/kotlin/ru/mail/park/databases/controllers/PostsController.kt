package ru.mail.park.databases.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mail.park.databases.dao.ForumDAO
import ru.mail.park.databases.dao.PostDAO
import ru.mail.park.databases.dao.ThreadDAO
import ru.mail.park.databases.dao.UserDAO
import ru.mail.park.databases.helpers.ApiHelper
import ru.mail.park.databases.models.Forum
import ru.mail.park.databases.models.Post
import ru.mail.park.databases.models.Thread
import ru.mail.park.databases.models.User

@RestController
@RequestMapping(
        path = [ApiHelper.POST_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class PostsController(private val forumDAO: ForumDAO,
                      private val postDAO: PostDAO,
                      private val threadDAO: ThreadDAO,
                      private val userDAO: UserDAO) {

    @PostMapping(path = ["{id}/details"])
    fun update(@PathVariable id: Int, @RequestBody postUpdateRequest: PostUpdateRequest): ResponseEntity<*> {
        postUpdateRequest.id = id
        val post = postDAO.update(postUpdateRequest)
        post?.forumSlug = forumDAO.getSlugById(post?.forumId!!)
        return ResponseEntity.status(HttpStatus.OK).body(post)
    }

    @GetMapping(path = ["{id}/details"])
    fun getDetails(@PathVariable id: Int,
                   @RequestParam(required = false, value = "related") related: List<String>?): ResponseEntity<*> {
        val post: Post? = postDAO.getById(id)
        post?.authorNickname = userDAO.getNickNameById(post?.authorId!!)
        post.forumSlug = forumDAO.getSlugById(post.forumId!!)

        var author: User? = null
        var forum: Forum? = null
        var thread: ru.mail.park.databases.models.Thread? = null

        if (related != null) {
            if (related.contains("author")) {
                author = userDAO.getById(post.authorId!!)
            }

            if (related.contains("forum")) {
                forum = forumDAO.getBySlugWithCounters(post.forumSlug!!)
                forum?.authorNickname = userDAO.getNickNameById(forum?.authorId!!)
            }

            if (related.contains("thread")) {
                thread = threadDAO.getById(post.threadId!!)
                thread?.authorNickname = userDAO.getNickNameById(thread?.authorId!!)
                thread.forumSlug = forumDAO.getSlugById(thread.forumId!!)
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(PostInfo(post, author, forum, thread))
    }

    data class PostCreateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "author") val authorNickname: String,
                @param:JsonProperty(value = "message") val message: String,
                @param:JsonProperty(value = "created", required = false) val createdAt: String?,
                @param:JsonProperty(value = "parent", required = false) val parentId: Int?)

    data class PostUpdateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "message") val message: String,
                @param:JsonProperty(value = "id", required = false) var id: Int)

    @Suppress("MemberVisibilityCanBePrivate")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class PostInfo(post: Post, author: User? = null, forum: Forum? = null, thread: ru.mail.park.databases.models.Thread? = null){

        @get:JsonProperty(value = "post")
        var post: Post = post

        @get:JsonProperty(value = "author")
        var author: User? = author

        @get:JsonProperty(value = "forum")
        var forum: Forum? = forum

        @get:JsonProperty(value = "thread")
        var thread: Thread? = thread
    }
}