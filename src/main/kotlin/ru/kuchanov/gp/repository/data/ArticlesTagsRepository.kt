package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.data.ArticlesTags
import javax.transaction.Transactional


interface ArticlesTagsRepository : JpaRepository<ArticlesTags, Long> {
    fun findByArticleIdAndTagId(articleId: Long, tagId: Long): ArticlesTags?

    fun findAllByArticleId(articleId: Long): List<ArticlesTags>

    @Transactional
    fun deleteAllByArticleId(articleId: Long)
}
