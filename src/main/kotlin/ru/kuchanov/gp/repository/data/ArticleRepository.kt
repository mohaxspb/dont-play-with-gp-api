package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.Article


interface ArticleRepository : JpaRepository<Article, Long> {

    fun findAllByAuthorId(authorId: Long): List<Article>

    @Query(
        """
            SELECT * FROM articles 
            WHERE published= :published AND approved= :approved
            ORDER BY published_date DESC 
            OFFSET :offset LIMIT :limit
        """,
        nativeQuery = true
    )
    fun getPublishedArticles(
        offset: Int,
        limit: Int,
        published: Boolean = true,
        approved: Boolean = true
    ): List<Article>
}
