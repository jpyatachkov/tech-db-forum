package ru.mail.park.databases.helpers

object UserDAOHelper {

    const val COUNT_USERS_QUERY = "" +
            "SELECT count(*)" +
            "FROM users";

    const val GET_BY_NICKNAME_QUERY = "" +
            "SELECT id, nickname, email, full_name, about " +
            "FROM users " +
            "WHERE nickname = ?::citext";

    const val GET_BY_NICKNAME_OR_EMAIL_QUERY = "" +
            "SELECT id, nickname, email, full_name, about " +
            "FROM users " +
            "WHERE nickname = ?::citext OR email = ?::citext";

    const val CREATE_USER_QUERY = "" +
            "INSERT INTO users (nickname, email, full_name, about) " +
            "VALUES (?, ?, ?, ?) " +
            "RETURNING id, nickname, email, full_name, about";

    const val UPDATE_USER_QUERY = "" +
            "UPDATE users " +
            "SET nickname = coalesce(?, nickname), " +
            "email = coalesce(?, email), " +
            "full_name = coalesce(?, full_name), " +
            "about = coalesce(?, about) " +
            "WHERE nickname = ?::citext " +
            "RETURNING id, nickname, email, full_name, about";
}