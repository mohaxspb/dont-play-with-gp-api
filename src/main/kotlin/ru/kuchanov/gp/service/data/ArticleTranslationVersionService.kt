package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.ArticleTranslationVersion

interface ArticleTranslationVersionService {
    fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun save(articleTranslationVersion: ArticleTranslationVersion): ArticleTranslationVersion
}
