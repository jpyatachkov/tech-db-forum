package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Thread(authorNickname: String?, message: String, title: String) {

    @get:JsonProperty(value = "id")
    var id: Int? = null

    @get:JsonProperty(value = "author")
    @set:JsonProperty(value = "author")
    var authorNickname: String? = authorNickname

    @get:JsonProperty
    @set:JsonProperty
    var message: String = message

    @get:JsonProperty
    @set:JsonProperty
    var title: String = title

    @get:JsonProperty(value = "created")
    var createdAt: String? = null

    @get:JsonProperty(value = "slug")
    var slug: String? = null

    @get:JsonProperty(value = "votes")
    var votesCount: Int? = null

    @get:JsonProperty(value = "forum")
    var forumSlug: String? = null

    constructor(id: Int?,
                authorNickname: String?,
                message: String,
                title: String,
                createdAt: String?,
                slug: String?,
                votesCount: Int?,
                forumSlug: String?): this(authorNickname, message, title) {
        this.id = id
        this.createdAt = createdAt
        this.slug = slug
        this.votesCount = votesCount
        this.forumSlug = forumSlug
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Thread

        if (id != other.id) return false
        if (authorNickname != other.authorNickname) return false
        if (message != other.message) return false
        if (title != other.title) return false
        if (createdAt != other.createdAt) return false
        if (slug != other.slug) return false
        if (votesCount != other.votesCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id ?: 0
        result = 31 * result + (authorNickname?.hashCode() ?: 0)
        result = 31 * result + message.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + (createdAt?.hashCode() ?: 0)
        result = 31 * result + (slug?.hashCode() ?: 0)
        result = 31 * result + (votesCount ?: 0)
        return result
    }
}