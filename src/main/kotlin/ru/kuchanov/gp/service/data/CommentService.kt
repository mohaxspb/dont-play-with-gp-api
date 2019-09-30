package ru.kuchanov.gp.service.data

import ru.kuchanov.gp.bean.data.Comment
import ru.kuchanov.gp.model.dto.data.CommentDto
import javax.transaction.Transactional

interface CommentService {

    fun getById(id: Long): Comment?

    fun getByIdAsDto(id: Long): CommentDto?

    fun findAllByAuthorId(authorId: Long): List<Comment>

    fun findAllByArticleIdAsDtoWithAuthor(articleId: Long, offset: Int, limit: Int): List<CommentDto>

    fun countCommentsForArticle(articleId: Long): Int

    fun save(comment: Comment): Comment

    @Transactional
    fun deleteById(id: Long): Boolean

    @Transactional
    fun deleteAllByArticleId(articleId: Long): Boolean
}
