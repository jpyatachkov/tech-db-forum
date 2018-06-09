package ru.mail.park.databases.controllers

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mail.park.databases.dao.UserDAO
import ru.mail.park.databases.exceptions.ConflictException
import ru.mail.park.databases.helpers.ApiHelper
import ru.mail.park.databases.models.User

@RestController
@RequestMapping(
        path = [ApiHelper.USER_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class UsersController(private val userDAO: UserDAO) {

    @GetMapping(path = ["{nickname}/profile"])
    fun getUser(@PathVariable nickname: String): ResponseEntity<User> {
        return ResponseEntity.ok(userDAO.getByNickName(nickname)!!);
    }

    @PostMapping(path = ["{nickname}/create"])
    fun createUser(@PathVariable nickname: String, @RequestBody request: UserCreateRequest): ResponseEntity<*> {
        val user: User?;
        request.nickname = nickname;
        return try {
            user = userDAO.create(request);
            ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (e: DuplicateKeyException) {
            e.printStackTrace();
            val users = userDAO.getByNickNameOrEmail(nickname, request.email);
            ResponseEntity.status(HttpStatus.CONFLICT).body(users);
        }
    }

    @PostMapping(path = ["{nickname}/profile"])
    fun updateUser(@PathVariable nickname: String, @RequestBody request: UserUpdateRequest): ResponseEntity<User> {
        return try {
            request.nickname = nickname;
            val user = userDAO.update(request);
            ResponseEntity.ok(user!!);
        } catch (e: DuplicateKeyException) {
            throw ConflictException("User with these key attributes already exists");
        }
    }

    data class UserCreateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "about", required = false) val about: String?,
                @param:JsonProperty(value = "email", required = true) val email: String,
                @param:JsonProperty(value = "fullname", required = true) val fullname: String,
                @param:JsonProperty(value = "nickname", required = false) var nickname: String?);

    data class UserUpdateRequest @JsonCreator
    constructor(@param:JsonProperty(value = "about", required = false) val about: String?,
                @param:JsonProperty(value = "email", required = false) val email: String?,
                @param:JsonProperty(value = "fullname", required = false) val fullname: String?,
                @param:JsonProperty(value = "nickname", required = false) var nickname: String?);
}