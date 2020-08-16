package ru.kuchanov.gp.model.dto.data


data class FeedDto(
    val articles: List<ArticleDto>,
    val totalSize: Int
)