package ru.mail.park.databases.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class InvalidRelation(public override val message: String): ApiException(message) {

}