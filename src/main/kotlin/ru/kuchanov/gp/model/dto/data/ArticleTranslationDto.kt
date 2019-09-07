package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.model.dto.UserDto
import java.sql.Timestamp


data class ArticleTranslationDto(
    val id: Long,

    val langId: Long,
    val articleId: Long,

    val title: String,
    val shortDescription: String?,
    val imageUrl: String?,

    val authorId: Long?,

    val approverId: Long? = null,
    val approved : Boolean = false,
    val approvedDate: Timestamp? = null,

    val publisherId: Long? = null,
    val published: Boolean = false,
    val publishedDate: Timestamp? = null,

    val created: Timestamp? = null,
    val updated: Timestamp? = null
) {
    var versions: List<ArticleTranslationVersionDto> = emptyList()
    var author: UserDto? = null
    var approver: UserDto? = null
    var publisher: UserDto? = null
}
