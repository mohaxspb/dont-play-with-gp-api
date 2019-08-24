package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.model.dto.ArticleDto
import ru.kuchanov.gp.service.data.ArticleService

@RestController
@RequestMapping("/" + GpConstants.ArticleEndpoint.PATH + "/")
class ArticleController @Autowired constructor(
    val articleService: ArticleService
) {

    @GetMapping
    fun index() =
        "Article endpoint"

    @PostMapping(GpConstants.ArticleEndpoint.Method.CREATE)
    fun createArticle(
        @AuthenticationPrincipal author: GpUser
        //todo
    ): ArticleDto {
        TODO()
    }

    @GetMapping(GpConstants.ArticleEndpoint.Method.ALL_BY_AUTHOR_ID)
    fun allArticlesByAuthorId(
        @RequestParam(value = "authorId") authorId: Long
    ) = articleService.findAllByAuthorId(authorId)
}
