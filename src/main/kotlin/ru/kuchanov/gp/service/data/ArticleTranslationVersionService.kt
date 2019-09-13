package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import javax.transaction.Transactional

interface ArticleTranslationVersionService {

    fun getOneById(id: Long): ArticleTranslationVersion?

    fun getOneByIdAsDto(id: Long): ArticleTranslationVersionDto?

    fun findAllByArticleTranslationIdAsDto(articleTranslationId: Long): List<ArticleTranslationVersionDto>

    fun findAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>

    fun findAllByAuthorId(authorId: Long): List<ArticleTranslationVersion>

    fun save(articleTranslationVersion: ArticleTranslationVersion): ArticleTranslationVersion

    @Transactional
    fun deleteById(id: Long): Boolean

    @Transactional
    fun deleteAllByArticleTranslationId(articleTranslationId: Long): List<ArticleTranslationVersion>
}
