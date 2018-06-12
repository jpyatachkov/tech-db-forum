package ru.mail.park.databases.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Suppress("MemberVisibilityCanBePrivate")
@JsonInclude(JsonInclude.Include.NON_NULL)
class User(fullName: String, email: String) {

    @JsonIgnore
    var id: Int? = null

    @get:JsonProperty(value = "about", required = false)
    @set:JsonProperty
    var about: String? = null

    @get:JsonProperty("nickname", required = false)
    @set:JsonProperty("nickname", required = false)
    var nickName: String? = null

    @NotBlank
    @get:JsonProperty
    @set:JsonProperty
    var email: String = email

    @NotBlank
    @get:JsonProperty("fullname")
    @set:JsonProperty("fullname")
    var fullName: String = fullName

    constructor(id: Int?, about: String?, nickName: String?, fullName: String, email: String): this(fullName, email) {
        this.id = id
        this.about = about
        this.nickName = nickName
    }
}