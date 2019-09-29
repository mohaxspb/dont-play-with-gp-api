package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.model.dto.UserDto
import java.sql.Timestamp


data class CommentDto(
    val id: Long,

    val text: String,

    val authorId: Long,
    val articleId: Long,

    val created: Timestamp?,
    val updated: Timestamp?
) {
    var author: UserDto? = null
}
