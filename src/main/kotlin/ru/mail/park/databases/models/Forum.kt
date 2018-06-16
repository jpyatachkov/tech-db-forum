package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Forum(authorNickname: String?, slug: String, title: String) {

    @get:JsonProperty
    @set:JsonProperty
    var slug: String = slug

    @get:JsonProperty
    @set:JsonProperty
    var title: String = title

    @get:JsonProperty("user")
    @set:JsonProperty("user")
    var authorNickname: String? = authorNickname

    @get:JsonProperty(value = "threads", access = JsonProperty.Access.READ_ONLY)
    var threadsCount: Int? = null

    @get:JsonProperty(value = "posts", access = JsonProperty.Access.READ_ONLY)
    var postsCount: Int? = null

    constructor(slug: String,
                title: String,
                threadsCount: Int?,
                postsCount: Int?,
                authorNickname: String?) : this(authorNickname, slug, title) {
        this.threadsCount = threadsCount
        this.postsCount = postsCount
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Forum

        if (title != other.title) return false
        if (slug != other.slug) return false
        if (authorNickname != other.authorNickname) return false
        if (threadsCount != other.threadsCount) return false
        if (postsCount != other.postsCount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + title.hashCode()
        result = 31 * result + slug.hashCode()
        result = 31 * result + (authorNickname?.hashCode() ?: 0)
        result = 31 * result + (threadsCount ?: 0)
        result = 31 * result + (postsCount ?: 0)
        return result
    }
}