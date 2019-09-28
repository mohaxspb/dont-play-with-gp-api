package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.Article
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import javax.transaction.Transactional

interface ArticleService {

    fun getOneById(id: Long): Article?

    fun getOneByIdAsDtoWithTranslationsAndVersions(id: Long): ArticleDto?

    fun findAllByAuthorId(authorId: Long): List<Article>

    /**
     * returns all [ArticleDto] with its [ArticleTranslationDto]s, where [authorId] is author of it
     * or for one of its [ArticleDto.translations]
     * or one of its [ArticleTranslationDto.versions]
     */
    fun findAllByAuthorIdWithTranslationsAsDto(
        authorId: Long,
        published: Boolean = true
    ): List<ArticleDto>

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean

    fun getPublishedArticles(
        offset: Int,
        limit: Int,
        published: Boolean = true,
        approved: Boolean = true,
        withTranslations: Boolean = true,
        withVersions: Boolean = false,
        onlyForCurrentDate: Boolean = true
    ): List<ArticleDto>

    fun save(article: Article): Article

    /**
     * delete article and its translations and their text versions
     */
    @Transactional
    fun deleteById(id: Long): Boolean
}
