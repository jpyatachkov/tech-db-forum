package ru.mail.park.databases.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Исключение во время работы приложения.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
open class ApiException(public override val message: String?) : RuntimeException() {
}