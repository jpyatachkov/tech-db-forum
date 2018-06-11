package ru.mail.park.databases.exceptions

class InvalidRelation(public override val message: String): ApiException(message) {

}