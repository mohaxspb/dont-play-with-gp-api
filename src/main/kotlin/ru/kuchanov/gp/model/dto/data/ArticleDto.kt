package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.model.dto.UserDto


data class ArticleDto(
    val id: Long,
    val authorId: Long?,
    val approverId: Long? = null,
    val publisherId: Long? = null,
    val originalLangId: Long,
    val sourceTitle: String?,
    val sourceUrl: String?,
    val sourceAuthorName: String?
) {
    var translations: List<ArticleTranslationDto> = emptyList()
    var author: UserDto? = null
    var approver: UserDto? = null
    var publisher: UserDto? = null
}
