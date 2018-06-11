package ru.mail.park.databases.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mail.park.databases.dao.ThreadDAO
import ru.mail.park.databases.helpers.ApiHelper
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.GetMapping
import ru.mail.park.databases.dao.ForumDAO
import ru.mail.park.databases.dao.UserDAO
import ru.mail.park.databases.dao.VoteDAO


@RestController
@RequestMapping(
        path = [ApiHelper.THREAD_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class ThreadsController(private val forumDAO: ForumDAO,
                        private val threadDAO: ThreadDAO,
                        private val userDAO: UserDAO,
                        private val voteDAO: VoteDAO) {

    @PostMapping(path = ["{slugOrId}/create"])
    fun createPosts(@PathVariable slugOrId: String,
                    @RequestBody createRequestBody: List<PostsController.PostCreateRequest>): ResponseEntity<*> {
        val thread = threadDAO.getBySlugOrId(slugOrId)
        val forumSlug = forumDAO.getSlugById(thread?.forumId!!)
        val posts = threadDAO.createRelatedPosts(forumSlug!!, thread, createRequestBody)
        return ResponseEntity.status(HttpStatus.CREATED).body(posts)
    }

    @GetMapping(path = ["{slugOrId}/details"])
    fun getDetails(@PathVariable slugOrId: String): ResponseEntity<*> {
        val thread = threadDAO.getBySlugOrId(slugOrId)
        thread?.authorNickname = userDAO.getNickNameById(thread?.authorId!!)
        thread.forumSlug = forumDAO.getSlugById(thread.forumId!!)
        return ResponseEntity.status(HttpStatus.OK).body(thread)
    }

    @PostMapping(path = ["{slugOrId}/details"])
    fun update(@PathVariable slugOrId: String, @RequestBody updateRequest: ThreadUpdateRequest): ResponseEntity<*> {
        updateRequest.slugOrId = slugOrId
        val thread = threadDAO.update(updateRequest)
        thread?.authorNickname = userDAO.getNickNameById(thread?.authorId!!)
        thread.forumSlug = forumDAO.getSlugById(thread.forumId!!)
        return ResponseEntity.status(HttpStatus.OK).body(thread)
    }

    @PostMapping(path = ["{slugOrId}/vote"])
    fun voteForThread(@PathVariable slugOrId: String,
                      @RequestBody threadVoteRequest: ThreadVoteRequest): ResponseEntity<*> {
        val threadId = threadDAO.getIdBySlugOrId(slugOrId)
        val userId = userDAO.getIdByNickName(threadVoteRequest.nickname)

        voteDAO.voteForThread(threadId!!, userId!!, threadVoteRequest.voice)

        val thread = threadDAO.getById(threadId)
        thread?.authorNickname = userDAO.getNickNameById(thread?.authorId!!)
        thread.forumSlug = forumDAO.getSlugById(thread.forumId!!)
        return ResponseEntity.status(HttpStatus.OK).body(thread)
    }

    data class ThreadCreateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "author") val authorNickname: String,
                @param:JsonProperty(value = "message") val message: String,
                @param:JsonProperty(value = "title") val title: String,
                @param:JsonProperty(value = "slug", required = false) val slug: String?,
                @param:JsonProperty(value = "created", required = false) val createdAt: String?)

    data class ThreadUpdateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "message", required = false) val message: String?,
                @param:JsonProperty(value = "title", required = false) val title: String?,
                @param:JsonProperty(value = "slug", required = false) var slugOrId: String?)

    data class ThreadVoteRequest
    constructor(@param:JsonProperty(value = "nickname") val nickname: String,
                @param:JsonProperty(value = "voice") val voice: Int)

//    @GetMapping(path = arrayOf("/{thread_slug_or_id}/posts"), consumes = arrayOf(MediaType.ALL_VALUE))
//    fun postsFromThread(@PathVariable thread_slug_or_id: String,
//                        @RequestParam(required = false, value = "limit") limit: Int?,
//                        @RequestParam(required = false, value = "since") since: Int?,
//                        @RequestParam(required = false, value = "sort") sort: String,
//                        @RequestParam(required = false, value = "desc") desc: Boolean?): ResponseEntity<*> {
//        var thread: Thread
//        val posts: List<Post>
//        try {
//            try {
//                val threadId = Integer.parseInt(thread_slug_or_id)
//                thread = threadDAO.getThreadById(threadId)
//            } catch (e: NumberFormatException) {
//                thread = threadDAO.getThreadBySlug(thread_slug_or_id)
//            }
//
//            posts = postDAO.getPostsFromThread(thread, limit, since, sort, desc)
//        } catch (e: Exceptions.NotFoundThread) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body<Any>(MessageResponse("Can't find thread by slug: $thread_slug_or_id"))
//        }
//
//        return ResponseEntity.ok<List<Post>>(posts)
//    }
//
//    @PostMapping(path = arrayOf("/{thread_slug_or_id}/vote"), consumes = arrayOf(MediaType.ALL_VALUE))
//    fun vote(@PathVariable thread_slug_or_id: String, @RequestBody request: VoteRequest): ResponseEntity<*> {
//        var thread: Thread
//        try {
//            try {
//                val threadId = Integer.parseInt(thread_slug_or_id)
//                thread = voteDAO.vote(request.getVoice(), threadId, request.getNickname())
//            } catch (e: NumberFormatException) {
//                thread = voteDAO.vote(request.getVoice(), thread_slug_or_id, request.getNickname())
//            }
//
//        } catch (e: DataIntegrityViolationException) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body<Any>(MessageResponse("Can't find thread or user: "))
//        }
//
//        return ResponseEntity.ok(thread)
//    }
}