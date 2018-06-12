package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.collections.ArrayList

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Post(authorNickname: String?, message: String, parentId: Int, createdAt: String?) {

    @JsonIgnore
    var authorId: Int? = null

    @JsonIgnore
    var materializedPath: ArrayList<Int> = ArrayList<Int>();

    @JsonIgnore
    var forumId: Int? = null

    @get:JsonProperty
    @set:JsonProperty
    var message: String = message

    @get:JsonProperty(value = "parent")
    @set:JsonProperty(value = "parent")
    var parentId: Int = parentId

    @get:JsonProperty(value = "author")
    @set:JsonProperty(value = "author")
    var authorNickname: String? = authorNickname

    @get:JsonProperty
    var id: Int? = null

    @get:JsonProperty(value = "isEdited")
    var isEdited: Boolean = false

    @get:JsonProperty(value = "created")
    var createdAt: String? = createdAt

    @get:JsonProperty(value = "thread")
    var threadId: Int? = null

    @get:JsonProperty(value = "forum")
    var forumSlug: String? = null

    constructor(id: Int?,
                isEdited: Boolean,
                message: String,
                parentId: Int,
                materializedPath: ArrayList<Int>,
                createdAt: String?,
                authorId: Int?,
                threadId: Int?,
                forumId: Int?) : this(null, message, parentId, createdAt) {
        this.id = id
        this.isEdited = isEdited
        this.materializedPath = materializedPath
        this.authorId = authorId
        this.threadId = threadId
        this.forumId = forumId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Post

        if (authorId != other.authorId) return false
        if (message != other.message) return false
        if (parentId != other.parentId) return false
        if (authorNickname != other.authorNickname) return false
        if (id != other.id) return false
        if (isEdited != other.isEdited) return false
        if (createdAt != other.createdAt) return false
        if (threadId != other.threadId) return false
        if (forumId != other.forumId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authorId ?: 0
        result = 31 * result + message.hashCode()
        result = 31 * result + parentId
        result = 31 * result + (authorNickname?.hashCode() ?: 0)
        result = 31 * result + (id ?: 0)
        result = 31 * result + isEdited.hashCode()
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (threadId ?: 0)
        result = 31 * result + (forumId ?: 0)
        return result
    }
}