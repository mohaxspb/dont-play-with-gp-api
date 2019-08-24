package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.ArticleTranslation

interface ArticleTranslationService {
    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>

    fun save(articleTranslation: ArticleTranslation): ArticleTranslation
}
