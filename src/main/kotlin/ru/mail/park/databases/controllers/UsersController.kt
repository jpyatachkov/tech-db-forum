package ru.mail.park.databases.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mail.park.databases.ApiException
import ru.mail.park.databases.dao.UserDAO
import ru.mail.park.databases.helpers.ApiHelper
import ru.mail.park.databases.models.User
import javax.validation.constraints.NotBlank

@RestController
@RequestMapping(
        path = [ApiHelper.USER_API_PATH],
        consumes = [MediaType.APPLICATION_JSON_UTF8_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class UsersController(private val userDAO: UserDAO) {

    @PostMapping(path = ["{nickname}/create"], consumes = [(MediaType.ALL_VALUE)])
    fun createUser(@PathVariable nickname: String, @RequestBody request: UserRequest): ResponseEntity<*> {
        val user: User?;
        request.nickname = nickname;
        return try {
            user = userDAO.create(request);
            ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (e: ApiException) {
            e.printStackTrace();
            val users = userDAO.getByNickNameOrEmail(nickname, request.email);
            ResponseEntity.status(HttpStatus.CONFLICT).body(users);
        }
    }

    @PostMapping(path = ["{nickname}/profile"], consumes = [(MediaType.ALL_VALUE)])
    fun updateUser(@PathVariable nickname: String, @RequestBody request: UserRequest): ResponseEntity<User> {
        return try {
            request.nickname = nickname;
            val user = userDAO.update(request);
            ResponseEntity.ok(user!!);
        } catch (e: ApiException) {
            e.printStackTrace();
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping(path = ["{nickname}/profile"], consumes = [(MediaType.ALL_VALUE)])
    fun getUser(@PathVariable nickname: String): ResponseEntity<User> {
        return try {
            ResponseEntity.ok(userDAO.getByNickName(nickname)!!);
        } catch (e: ApiException) {
            e.printStackTrace();
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    data class UserRequest @JsonCreator
    constructor(@param:JsonProperty(value = "about") val about: String,
                @param:JsonProperty(value = "email") @field:NotBlank val email: String,
                @param:JsonProperty(value = "fullname") @field:NotBlank val fullname: String,
                @param:JsonProperty(value = "nickname") var nickname: String?)
}