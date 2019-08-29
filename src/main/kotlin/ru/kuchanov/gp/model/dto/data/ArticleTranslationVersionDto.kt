package ru.kuchanov.gp.model.dto.data


data class ArticleTranslationVersionDto(
    val id: Long,
    val authorId: Long?,
    val articleTranslationId: Long,
    val text: String
)
