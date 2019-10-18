package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.ArticleTranslation


interface ArticleTranslationRepository : JpaRepository<ArticleTranslation, Long> {

    fun findAllByArticleId(articleId: Long): List<ArticleTranslation>

    @Query(
        """
            SELECT * FROM article_translations 
            WHERE id NOT IN (:excludedIds) 
            AND created >= CAST(:startDate AS timestamp) 
            AND created <= CAST(:endDate AS timestamp) 
            ORDER BY created
        """,
        nativeQuery = true
    )
    fun getCreatedTranslationsBetweenDates(
        startDate: String,
        endDate: String,
        //as we are not allowed to pass empty list to JPA query
        excludedIds: List<Long> = listOf(0)
    ): List<ArticleTranslation>

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

    fun countByArticleId(articleId: Long): Int

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean
}
