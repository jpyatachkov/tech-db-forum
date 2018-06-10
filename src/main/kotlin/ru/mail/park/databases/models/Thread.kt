package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Thread(authorNickname: String?, message: String, title: String) {

    @JsonIgnore
    var id: Int? = null

    @JsonIgnore
    var authorId: Int? = null

    @get:JsonProperty(value = "author")
    @set:JsonProperty(value = "author")
    var authorNickname: String? = authorNickname

    @get:JsonProperty
    @set:JsonProperty
    var message: String = message

    @get:JsonProperty
    @set:JsonProperty
    var title: String = title

    @get:JsonProperty(value = "created", access = JsonProperty.Access.READ_ONLY)
    var createdAt: Date? = null

    @get:JsonProperty(value = "slug", access = JsonProperty.Access.READ_ONLY)
    var slug: String? = null

    @get:JsonProperty(value = "votes", access = JsonProperty.Access.READ_ONLY)
    var votesCount: Int? = null

    @get:JsonProperty(value = "forum", access = JsonProperty.Access.READ_ONLY)
    var forumId: Int? = null

    constructor(id: Int?,
                authorId: Int?,
                message: String,
                title: String,
                createdAt: Date?,
                slug: String?,
                votesCount: Int?,
                forumId: Int?): this(null, message, title) {
        this.id = id
        this.authorId = authorId
        this.createdAt = createdAt
        this.slug = slug
        this.votesCount = votesCount
        this.forumId = forumId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Thread

        if (id != other.id) return false
        if (authorId != other.authorId) return false
        if (authorNickname != other.authorNickname) return false
        if (message != other.message) return false
        if (title != other.title) return false
        if (createdAt != other.createdAt) return false
        if (slug != other.slug) return false
        if (votesCount != other.votesCount) return false
        if (forumId != other.forumId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (authorId ?: 0)
        result = 31 * result + (authorNickname?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (slug?.hashCode() ?: 0)
        result = 31 * result + (votesCount ?: 0)
        result = 31 * result + (forumId ?: 0)
        return result
    }
}