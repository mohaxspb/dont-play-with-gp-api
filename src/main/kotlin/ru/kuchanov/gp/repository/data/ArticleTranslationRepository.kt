package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.ArticleTranslation


interface ArticleTranslationRepository : JpaRepository<ArticleTranslation, Long> {
    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>

    @Query("select articleId from ArticleTranslation where id=:id")
    fun getArticleIdById(id: Long): Long

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean
}
