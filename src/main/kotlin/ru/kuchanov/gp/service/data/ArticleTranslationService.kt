package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.ArticleTranslation
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import javax.transaction.Transactional

interface ArticleTranslationService {
    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>

    fun findAllByArticleIdAsDtoWithVersions(articleId: Long): List<ArticleTranslationDto>

    fun save(articleTranslation: ArticleTranslation): ArticleTranslation

    /**
     * delete translations and its text versions
     */
    @Transactional
    fun deleteById(id: Long): Boolean

    /**
     * delete all translations and their text versions
     */
    @Transactional
    fun deleteAllByArticleId(articleId: Long): Boolean
}
