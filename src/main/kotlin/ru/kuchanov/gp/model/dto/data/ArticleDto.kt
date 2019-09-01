package ru.kuchanov.gp.model.dto.data


data class ArticleDto(
    val id: Long,
    val authorId: Long?,
    val originalLangId: Long,
    val sourceTitle: String?,
    val sourceUrl: String?,
    val sourceAuthorName: String?
) {
    var translations: List<ArticleTranslationDto> = emptyList()
}
