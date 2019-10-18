package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import javax.transaction.Transactional

interface ArticleTranslationVersionService {

    fun getOneById(id: Long): ArticleTranslationVersion?

    fun getOneByIdAsDto(id: Long): ArticleTranslationVersionDto?

    fun getCreatedVersionsBetweenDates(
        startDate: String,
        endDate: String,
        //as we are not allowed to pass empty list to JPA query
        excludedIds: List<Long> = listOf(0)
    ): List<ArticleTranslationVersionDto>

    fun getPublishedByTranslationId(translationId: Long): ArticleTranslationVersion?

    fun findAllByArticleTranslationIdAsDto(articleTranslationId: Long): List<ArticleTranslationVersionDto>

    fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun findAllByAuthorId(authorId: Long): List<ArticleTranslationVersion>

    fun countOfVersionsByVersionId(versionId: Long): Int

    fun countByArticleId(articleId: Long): Int

    fun isUserIsAuthorOfVersionOrTranslationOrArticleByVersionId(versionId: Long, userId: Long): Boolean

    fun save(articleTranslationVersion: ArticleTranslationVersion): ArticleTranslationVersion

    @Transactional
    fun deleteById(id: Long): Boolean

    @Transactional
    fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>
}
