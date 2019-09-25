package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.ArticleTranslation


interface ArticleTranslationRepository : JpaRepository<ArticleTranslation, Long> {
    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>

    @Query("select articleId from ArticleTranslation where id=:id")
    fun getArticleIdById(id: Long): Long?

    @Query(
        """
            select count(*) from article_translations 
            where article_id = (select article_id from article_translations where id=:translationId)
        """,
        nativeQuery = true
    )
    fun countTranslationsByTranslationId(translationId: Long): Int

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean
}
