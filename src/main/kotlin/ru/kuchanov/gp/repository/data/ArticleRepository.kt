package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.Article


interface ArticleRepository : JpaRepository<Article, Long> {

    fun findAllByAuthorId(authorId: Long): List<Article>

    /**
     * Returns articles list, published after provided article id (inclusive)
     */
    @Query(
        """
            SELECT * FROM articles 
            WHERE id >= :startArticleId 
            AND published= TRUE 
        """,
        nativeQuery = true
    )
    fun getPublishedArticlesAfterId(startArticleId: Long): List<Article>

    @Query(
        """
            SELECT * FROM articles 
            WHERE published= :published 
            AND approved= :approved 
            AND published_date <= CAST( :publishDate AS timestamp) 
            ORDER BY published_date DESC 
            OFFSET :offset LIMIT :limit
        """,
        nativeQuery = true
    )
    fun getPublishedArticles(
        offset: Int,
        limit: Int,
        published: Boolean = true,
        approved: Boolean = true,
        publishDate: String
    ): List<Article>

    @Query(
        """
            SELECT count(*) FROM articles 
            WHERE published= :published 
            AND approved= :approved 
            AND published_date <= CAST( :publishDate AS timestamp) 
        """,
        nativeQuery = true
    )
    fun getPublishedArticlesCount(
        published: Boolean = true,
        approved: Boolean = true,
        publishDate: String
    ): Int

    @Query(
        """
            SELECT * FROM articles 
            WHERE created >= CAST( :startDate AS timestamp) 
            AND created <= CAST( :endDate AS timestamp) 
            ORDER BY created
        """,
        nativeQuery = true
    )
    fun getCreatedArticlesBetweenDates(startDate: String, endDate: String): List<Article>

    @Query(
        """
            select * from articles as a 
            where a.id in (
              select article_id from article_translations as t where t.id in (
                  select v.article_translation_id from article_translation_versions as v where v.author_id = :authorId
                ) or t.author_id = :authorId
            ) or a.author_id = :authorId 
            order by published
        """,
        nativeQuery = true
    )
    fun findAllContentAuthorId(authorId: Long): List<Article>

    fun existsByIdAndAuthorId(id: Long, authorId: Long): Boolean
}
