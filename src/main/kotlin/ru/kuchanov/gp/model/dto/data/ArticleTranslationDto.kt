package ru.kuchanov.gp.model.dto.data


data class ArticleTranslationDto(
    val id: Long,
    val authorId: Long?,
    val langId: Long,
    val articleId: Long,
    val title: String,
    val shortDescription: String?,
    val imageUrl: String?
) {
    var versions: List<ArticleTranslationVersionDto> = emptyList()
}
