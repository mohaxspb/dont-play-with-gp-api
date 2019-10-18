package ru.kuchanov.gp.model.dto.data

interface ArticleIdAndCommentsCount {

    fun getArticleId(): Long
    fun getCommentsCount(): Long
}