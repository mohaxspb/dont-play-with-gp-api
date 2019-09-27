package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.Tag
import javax.transaction.Transactional

interface TagService {
    fun getOneById(id: Long): Tag?

    fun findByTitle(title: String): Tag?

    fun findAll(): List<Tag>

    fun save(tag: Tag): Tag

    @Transactional
    fun delete(tag: Tag): Boolean

    @Transactional
    fun deleteAllByArticleId(articleId: Long)

    fun saveTagsForArticle(tags: List<Tag>, articleId: Long, authorId: Long)

    fun findAllForArticle(articleId: Long): List<Tag>
}
