package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.ArticleTranslation
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import javax.transaction.Transactional

interface ArticleTranslationService {

    fun getOneById(id: Long): ArticleTranslation?

    fun getOneByIdAsDtoWithVersions(id: Long): ArticleTranslationDto?

    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>

    fun findAllByArticleIdAsDtoWithVersions(articleId: Long): List<ArticleTranslationDto>

    fun findAllByArticleIdAsDto(articleId: Long): List<ArticleTranslationDto>

    fun isUserIsAuthorOfTranslationOrArticleByTranslationId(translationId: Long, userId: Long): Boolean

    fun countOfTranslationsByTranslationId(translationId: Long): Int

    fun countTranslationsByArticleId(articleId: Long): Int

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean

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
