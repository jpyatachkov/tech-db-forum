package ru.mail.park.databases.controllers

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mail.park.databases.dao.ThreadDAO
import ru.mail.park.databases.helpers.ApiHelper
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.GetMapping
import ru.mail.park.databases.models.Post
import java.util.ArrayList



@RestController
@RequestMapping(
        path = [ApiHelper.THREAD_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class ThreadsController(private val threadDAO: ThreadDAO) {

//    @PostMapping(path = ["/{slugOrId}/create"])
//    fun createPosts(@PathVariable slugOrId: String, @RequestBody request: List<Post>): ResponseEntity<*> {
//        var posts: MutableList<Post> = ArrayList<Post>()
//        var thread = threadDAO.getBySlugOrId(slugOrId)
//
//        for (post in request) {
//            post.threadId = thread?.id
//            post.forumId = thread?.forumId
//            posts.add(post)
//        }
//        try {
//            posts = postDAO.createPosts(posts)
//        } catch (e: Exceptions.InvalidParrent) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body<Any>(MessageResponse("Parent post was created in another thread"))
//        } catch (e: Exceptions.NotFoundUser) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body<Any>(MessageResponse("Can't find post author by nickname"))
//        } catch (e: Exceptions.NotFoundThread) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body<Any>(MessageResponse("Can't find post thread by id"))
//        }
//
//        return ResponseEntity.status(HttpStatus.CREATED).body<List<Post>>(posts)
//    }
//
//    @GetMapping(path = arrayOf("/{thread_slug_or_id}/details"), consumes = arrayOf(MediaType.ALL_VALUE))
//    fun getDetails(@PathVariable thread_slug_or_id: String): ResponseEntity<*> {
//        var thread: Thread
//        try {
//            try {
//                val threadId = Integer.parseInt(thread_slug_or_id)
//                thread = threadDAO.getThreadById(threadId)
//            } catch (e: NumberFormatException) {
//                thread = threadDAO.getThreadBySlug(thread_slug_or_id)
//            }
//
//        } catch (e: Exceptions.NotFoundThread) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body<Any>(MessageResponse("Can't find thread with slug$thread_slug_or_id"))
//        }
//
//        return ResponseEntity.ok().body(thread)
//    }
//
//    @PostMapping(path = arrayOf("/{thread_slug_or_id}/details"), consumes = arrayOf(MediaType.ALL_VALUE))
//    fun updateThread(@PathVariable thread_slug_or_id: String, @RequestBody request: ThreadRequest): ResponseEntity<*> {
//        var thread: Thread? = null
//        try {
//            try {
//                try {
//                    val threadId = Integer.parseInt(thread_slug_or_id)
//                    threadDAO.updateThread(threadId, request.getAuthor(), request.getCreated(), request.getMessage(), request.getTitle())
//                    thread = threadDAO.getThreadById(threadId)
//                } catch (e: NumberFormatException) {
//                    threadDAO.updateThread(thread_slug_or_id, request.getAuthor(), request.getCreated(), request.getMessage(), request.getTitle())
//                    thread = threadDAO.getThreadBySlug(thread_slug_or_id)
//                }
//
//            } catch (e: Exceptions.NotModified) {
//                try {
//                    val threadId = Integer.parseInt(thread_slug_or_id)
//                    thread = threadDAO.getThreadById(threadId)
//                } catch (e1: NumberFormatException) {
//                    thread = threadDAO.getThreadBySlug(thread_slug_or_id)
//                }
//
//            }
//
//        } catch (e: Exceptions.NotFoundThread) {
//            thread = null
//        }
//
//        if (thread == null) {
//            val resp = MessageResponse("Can't find forum")
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body<Any>(resp)
//        }
//        return ResponseEntity.ok(thread)
//    }
//
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