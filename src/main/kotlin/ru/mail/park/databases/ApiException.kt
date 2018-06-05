package ru.mail.park.databases

/**
 * Исключение во время работы приложения.
 */
class ApiException(public val what: String?) : RuntimeException() {
}