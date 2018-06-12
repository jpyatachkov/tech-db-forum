package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class Vote(authorNickname: String?, voice: Int) {

    @JsonIgnore
    var authorId: Int? = null

    @get:JsonProperty(value = "nickname")
    @set:JsonProperty(value = "nickname")
    var authorNickname: String? = authorNickname

    @get:JsonProperty
    @set:JsonProperty
    var voice: Int = voice

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vote

        if (authorId != other.authorId) return false
        if (authorNickname != other.authorNickname) return false
        if (voice != other.voice) return false

        return true
    }

    override fun hashCode(): Int {
        var result = authorId ?: 0
        result = 31 * result + (authorNickname?.hashCode() ?: 0)
        result = 31 * result + voice
        return result
    }
}