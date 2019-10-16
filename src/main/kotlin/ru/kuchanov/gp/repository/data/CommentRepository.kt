package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.Comment
import ru.kuchanov.gp.model.dto.data.ArticleIdAndCommentsCount
import javax.transaction.Transactional


interface CommentRepository : JpaRepository<Comment, Long> {
    fun findAllByAuthorId(authorId: Long): List<Comment>

    @Query(
        """
            SELECT * FROM comments 
            WHERE article_id= :articleId 
            ORDER BY created DESC 
            OFFSET :offset LIMIT :limit
        """,
        nativeQuery = true
    )
    fun getByArticleIdWithOffsetAndLimit(
        articleId: Long,
        offset: Int,
        limit: Int
    ): List<Comment>

    @Transactional
    fun deleteAllByArticleId(articleId: Long): Boolean

    fun countByArticleId(articleId: Long):Int

    @Query(
        """
            SELECT article_id FROM comments
            WHERE created >= CAST( :startDate AS timestamp) 
            AND created <= CAST( :endDate AS timestamp) 
        """,
        nativeQuery = true
    )
    fun getArticleIdsForCommentsCreatedBetweenDates(startDate: String, endDate: String): List<Long>

    @Query(
        """
            SELECT article_id as articleId, count(created) as commentsCount FROM comments
            WHERE created >= CAST( :startDate AS timestamp) 
            AND created <= CAST( :endDate AS timestamp) 
            GROUP BY article_id
        """,
        nativeQuery = true
    )
    fun getArticleIdsAndCommentsCountForCommentsCreatedBetweenDates(
        startDate: String,
        endDate: String
    ): List<ArticleIdAndCommentsCount>
}
