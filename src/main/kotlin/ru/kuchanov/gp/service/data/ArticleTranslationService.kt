package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.ArticleTranslation
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto

interface ArticleTranslationService {
    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>

    fun findAllByArticleIdAsDtoWithVersions(articleId: Long): List<ArticleTranslationDto>

    fun save(articleTranslation: ArticleTranslation): ArticleTranslation
}
