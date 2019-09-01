package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.model.dto.UserDto


data class ArticleTranslationDto(
    val id: Long,
    val authorId: Long?,
    val approverId: Long? = null,
    val publisherId: Long? = null,
    val langId: Long,
    val articleId: Long,
    val title: String,
    val shortDescription: String?,
    val imageUrl: String?
) {
    var versions: List<ArticleTranslationVersionDto> = emptyList()
    var author: UserDto? = null
    var approver: UserDto? = null
    var publisher: UserDto? = null
}
