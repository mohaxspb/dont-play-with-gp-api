package ru.kuchanov.gp.service.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.toDto
import ru.kuchanov.gp.bean.data.Comment
import ru.kuchanov.gp.bean.data.toDto
import ru.kuchanov.gp.model.dto.data.ArticleIdAndCommentsCount
import ru.kuchanov.gp.model.dto.data.CommentDto
import ru.kuchanov.gp.repository.data.CommentRepository
import ru.kuchanov.gp.service.auth.GpUserDetailsService

@Service
class CommentServiceImpl @Autowired constructor(
    val commentRepository: CommentRepository,
    val userService: GpUserDetailsService
) : CommentService {

    override fun getById(id: Long): Comment? =
        commentRepository.findByIdOrNull(id)

    override fun getByIdAsDto(id: Long): CommentDto? =
        commentRepository.findByIdOrNull(id)?.toDto()?.withAuthor()

    override fun findAllByAuthorId(authorId: Long): List<Comment> =
        commentRepository.findAllByAuthorId(authorId)

    override fun findAllByArticleIdAsDtoWithAuthor(articleId: Long, offset: Int, limit: Int): List<CommentDto> =
        commentRepository.getByArticleIdWithOffsetAndLimit(articleId, offset, limit).map { it.toDto().withAuthor() }

    override fun countCommentsForArticle(articleId: Long): Int =
        commentRepository.countByArticleId(articleId)

    override fun save(comment: Comment): Comment =
        commentRepository.save(comment)

    override fun deleteById(id: Long): Boolean {
        commentRepository.deleteById(id)
        return true
    }

    override fun deleteAllByArticleId(articleId: Long): Boolean =
        commentRepository.deleteAllByArticleId(articleId)

    override fun getArticleIdsForCommentsCreatedBetweenDates(startDate: String, endDate: String): List<Long> =
        commentRepository.getArticleIdsForCommentsCreatedBetweenDates(startDate, endDate)

    override fun getArticleIdsAndCommentsCountForCommentsCreatedBetweenDates(
        startDate: String,
        endDate: String
    ): List<ArticleIdAndCommentsCount> =
        commentRepository.getArticleIdsAndCommentsCountForCommentsCreatedBetweenDates(startDate, endDate)

    fun CommentDto.withAuthor() =
        apply {
            author = userService.getById(authorId)?.toDto()
        }
}
