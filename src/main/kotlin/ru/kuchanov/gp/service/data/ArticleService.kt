package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.Article
import ru.kuchanov.gp.model.dto.data.ArticleDto
import javax.transaction.Transactional

interface ArticleService {

    fun getOneById(id: Long): Article?

    fun getOneByIdAsDtoWithTranslationsAndVersions(id: Long): ArticleDto?

    fun findAllByAuthorId(authorId: Long): List<Article>

    fun save(article: Article): Article

    /**
     * delete article and its translations and their text versions
     */
    @Transactional
    fun deleteById(id: Long): Boolean
}
