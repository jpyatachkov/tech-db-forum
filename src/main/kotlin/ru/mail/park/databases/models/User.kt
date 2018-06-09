package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class User(@NotNull id: Int?, fullName: String, email: String, nickName: String, about: String) {

    companion object {
        const val PRIME = 31;
    }

    @JsonIgnore
    var id: Int? = id

    @get:JsonProperty
    @set:JsonProperty
    var about: String? = about

    @NotBlank
    @get:JsonProperty
    @set:JsonProperty
    var email: String? = email

    @NotBlank
    @get:JsonProperty("fullname")
    @set:JsonProperty("fullname")
    var fullName: String? = fullName

    @get:JsonProperty("nickname")
    @set:JsonProperty("nickname")
    var nickName: String? = nickName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val user = other as User?

        if (id != null && id == user!!.id) return true
        return nickName != null && nickName == user!!.nickName
    }

    override fun hashCode(): Int {
        var result = if (fullName != null) fullName!!.hashCode() else 0
        result = PRIME * result + if (email != null) email!!.hashCode() else 0
        result = PRIME * result + if (nickName != null) nickName!!.hashCode() else 0
        result = PRIME * result + if (about != null) about!!.hashCode() else 0
        return result
    }
}