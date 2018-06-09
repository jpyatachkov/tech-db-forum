package ru.mail.park.databases.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.mail.park.databases.dao.ServiceDAO
import ru.mail.park.databases.helpers.ApiHelper

@RestController
@RequestMapping(
        path = [ApiHelper.SERVICE_API_PATH],
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_JSON_UTF8_VALUE]
)
class ServiceController(private val serviceDAO: ServiceDAO) {

    @PostMapping(path = ["clear"])
    fun doClear(): ResponseEntity<*> {
        serviceDAO.doClear();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @GetMapping(path = ["status"])
    fun getStatus(): ResponseEntity<*> {
        return ResponseEntity.ok(serviceDAO.getStatus());
    }
}