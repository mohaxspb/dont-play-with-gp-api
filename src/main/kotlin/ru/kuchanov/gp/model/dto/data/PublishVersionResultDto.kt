package ru.kuchanov.gp.model.dto.data

data class PublishVersionResultDto(
    val updatedVersion: ArticleTranslationVersionDto,
    val unpublishedVersion: ArticleTranslationVersionDto?
)
