package ru.mail.park.databases.helpers

import java.text.SimpleDateFormat
import java.util.*

object DateTimeHelper {

    private fun toISODate(date: Date): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        sdf.timeZone = TimeZone.getTimeZone("MSK")
        return sdf.format(date)
    }

    fun toISODate(): String {
        return toISODate(Date(System.currentTimeMillis()))
    }
}