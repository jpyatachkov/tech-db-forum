package ru.mail.park.databases.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mail.park.databases.dao.ForumDAO
import ru.mail.park.databases.dao.PostDAO
import ru.mail.park.databases.helpers.ApiHelper

@RestController
@RequestMapping(
        path = [ApiHelper.POST_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class PostsController(private val forumDAO: ForumDAO, private val postDAO: PostDAO) {

    @PostMapping(path = ["{id}/details"])
    fun update(@PathVariable id: Int, @RequestBody postUpdateRequest: PostUpdateRequest): ResponseEntity<*> {
        postUpdateRequest.id = id
        val post = postDAO.update(postUpdateRequest)
        post?.forumSlug = forumDAO.getSlugById(post?.forumId!!)
        return ResponseEntity.status(HttpStatus.OK).body(post)
    }

    @GetMapping(path = ["{id}/details"])
    fun getDetails(@PathVariable id: Int): ResponseEntity<*> {
        val post = postDAO.getById(id)
        post?.forumSlug = forumDAO.getSlugById(post?.forumId!!)
        return ResponseEntity.status(HttpStatus.OK).body(post)
    }

    data class PostCreateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "author") val authorNickname: String,
                @param:JsonProperty(value = "message") val message: String,
                @param:JsonProperty(value = "created", required = false) val createdAt: String?,
                @param:JsonProperty(value = "parent", required = false) val parentId: Int?)

    data class PostUpdateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "message") val message: String,
                @param:JsonProperty(value = "id", required = false) var id: Int)
}