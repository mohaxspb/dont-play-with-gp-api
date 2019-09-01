package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.model.dto.UserDto


data class ArticleTranslationVersionDto(
    val id: Long,
    val authorId: Long?,
    val approverId: Long? = null,
    val publisherId: Long? = null,
    val articleTranslationId: Long,
    val text: String
) {
    var author: UserDto? = null
    var approver: UserDto? = null
    var publisher: UserDto? = null
}
