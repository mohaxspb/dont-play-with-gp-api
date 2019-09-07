package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.model.dto.UserDto
import java.sql.Timestamp


data class ArticleTranslationVersionDto(
    val id: Long,

    val articleTranslationId: Long,

    val text: String,

    val authorId: Long?,

    val approverId: Long? = null,
    val approved: Boolean = false,
    val approvedDate: Timestamp? = null,

    val publisherId: Long? = null,
    val published: Boolean = false,
    val publishedDate: Timestamp? = null,

    val created: Timestamp? = null,
    val updated: Timestamp? = null
) {
    var author: UserDto? = null
    var approver: UserDto? = null
    var publisher: UserDto? = null
}
