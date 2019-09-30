package ru.kuchanov.gp.repository.data

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import ru.kuchanov.gp.bean.data.Comment
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
}
