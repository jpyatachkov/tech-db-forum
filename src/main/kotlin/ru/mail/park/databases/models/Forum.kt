package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.jetbrains.annotations.NotNull

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Forum(@NotNull id: Int?, title: String, slug: String, threadsCount: Int? = null, postsCount: Int? = null, authorId: Int) {

    companion object {
        const val PRIME = 31;
    }

    @JsonIgnore
    var id: Int? = id;

    @get:JsonProperty
    @set:JsonProperty
    var title: String = title;

    @get:JsonProperty
    @set:JsonProperty
    var slug: String = slug;

    @get:JsonProperty("user")
    @set:JsonProperty("user")
    var authorNickname: String? = null;

    @JsonIgnore
    var threadsCount: Int? = threadsCount;

    @JsonIgnore
    var postsCount: Int? = postsCount;

    @JsonIgnore
    var authorId: Int = authorId;

    override fun equals(other: Any?): Boolean {
        if (this === other) return true;
        if (javaClass != other?.javaClass) return false;

        other as Forum;

        if (id != other.id) return false;
        if (title != other.title) return false;
        if (slug != other.slug) return false;
        if (threadsCount != other.threadsCount) return false;
        if (postsCount != other.postsCount) return false;
        if (authorId != other.authorId) return false;

        return true;
    }

    override fun hashCode(): Int {
        var result = id ?: 0;
        result = PRIME * result + title.hashCode();
        result = PRIME * result + slug.hashCode();
        result = PRIME * result + if (threadsCount != null) threadsCount!!.hashCode() else 0;
        result = PRIME * result + if (postsCount != null) postsCount!!.hashCode() else 0;
        result = PRIME * result + authorId.hashCode();
        return result;
    }
}