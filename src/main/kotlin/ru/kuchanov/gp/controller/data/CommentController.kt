package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.bean.data.ArticleNotFoundException
import ru.kuchanov.gp.bean.data.Comment
import ru.kuchanov.gp.bean.data.CommentNotFoundException
import ru.kuchanov.gp.model.dto.data.CommentDto
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.data.ArticleService
import ru.kuchanov.gp.service.data.CommentService

@RestController
@RequestMapping("/" + GpConstants.CommentEndpoint.PATH + "/")
class CommentController @Autowired constructor(
    val commentService: CommentService,
    val articleService: ArticleService
) {
    @GetMapping
    fun index() =
        "Comment endpoint"

    @GetMapping(GpConstants.CommentEndpoint.Method.COUNT_FOR_ARTICLE)
    fun countForArticle(@RequestParam(value = "articleId") articleId: Long): Int =
        commentService.countCommentsForArticle(articleId)

    @GetMapping(GpConstants.CommentEndpoint.Method.ALL)
    fun getAll(
        @RequestParam(value = "articleId") articleId: Long,
        @RequestParam(value = "offset") offset: Int,
        @RequestParam(value = "limit") limit: Int
    ): List<CommentDto> =
        commentService.findAllByArticleIdAsDtoWithAuthor(articleId, offset, limit)

    @GetMapping(GpConstants.CommentEndpoint.Method.ALL_BY_AUTHOR_ID)
    fun getAllForUser(
        @RequestParam(value = "userId") userId: Long
    ): List<Comment> =
        commentService.findAllByAuthorId(userId)

    @PostMapping(GpConstants.CommentEndpoint.Method.ADD)
    fun create(
        @RequestParam(value = "articleId") articleId: Long,
        @RequestParam(value = "text") text: String,
        @AuthenticationPrincipal author: GpUser
    ): CommentDto {
        if (articleService.getOneById(articleId) != null) {
            val createdComment =
                commentService.save(
                    Comment(
                        text = text,
                        authorId = author.id!!,
                        articleId = articleId
                    )
                )
            return commentService.getByIdAsDto(createdComment.id!!)!!
        } else {
            throw ArticleNotFoundException()
        }
    }

    @DeleteMapping(GpConstants.CommentEndpoint.Method.DELETE + "/{id}")
    fun delete(
        @PathVariable(value = "id") id: Long,
        @AuthenticationPrincipal user: GpUser
    ): Boolean {
        val comment = commentService.getByIdAsDto(id) ?: throw CommentNotFoundException()
        if (user.isAdmin() || user.id!! == comment.authorId) {
            return commentService.deleteById(id)
        } else {
            throw GpAccessDeniedException("You are not author or admin of this comment!")
        }
    }
}
