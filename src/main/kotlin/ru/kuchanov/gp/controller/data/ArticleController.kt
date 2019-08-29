package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.data.Article
import ru.kuchanov.gp.bean.data.ArticleTranslation
import ru.kuchanov.gp.bean.data.ArticleTranslationVersion
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.service.data.ArticleService
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService

@RestController
@RequestMapping("/" + GpConstants.ArticleEndpoint.PATH + "/")
class ArticleController @Autowired constructor(
    val articleService: ArticleService,
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService
) {

    @GetMapping
    fun index() =
        "Article endpoint"

    @GetMapping(GpConstants.ArticleEndpoint.Method.ALL_BY_AUTHOR_ID)
    fun allArticlesByAuthorId(
        @RequestParam(value = "authorId") authorId: Long
    ) = articleService.findAllByAuthorId(authorId)

    @GetMapping("{id}")
    fun getById(@PathVariable(name = "id") id: Long) =
        articleService.getOneById(id)

    @PostMapping(GpConstants.ArticleEndpoint.Method.CREATE)
    fun createArticle(
        @RequestParam(value = "sourceTitle") sourceTitle: String?,
        @RequestParam(value = "sourceAuthorName") sourceAuthorName: String?,
        @RequestParam(value = "sourceUrl") sourceUrl: String?,
        //todo image param
        @RequestParam(value = "articleLanguageId") articleLanguageId: Long,
        @RequestParam(value = "title") title: String,
        @RequestParam(value = "shortDescription") shortDescription: String?,
        @RequestParam(value = "text") text: String,
        @AuthenticationPrincipal author: GpUser
    ): ArticleDto {
        println("createArticle: $articleLanguageId, $sourceTitle, $sourceAuthorName, $sourceUrl, $title, $shortDescription, $text")
        val authorId = author.id!!
        //save article
        val article = Article(
            authorId = authorId,
            originalLangId = articleLanguageId,
            sourceTitle = sourceTitle,
            sourceAuthorName = sourceAuthorName,
            sourceUrl = sourceUrl
        )
        val articleInDb = articleService.save(article)
        //save article translation
        val articleTranslation = ArticleTranslation(
            authorId = authorId,
            langId = articleLanguageId,
            articleId = articleInDb.id!!,
            //todo imageUrl
            imageUrl = null,
            shortDescription = shortDescription,
            title = title
        )
        val articleTranslationInDb = articleTranslationService.save(articleTranslation)
        //save article translation version
        val textVersion = ArticleTranslationVersion(
            authorId = authorId,
            articleTranslationId = articleTranslationInDb.id!!,
            text = text
        )
        val textVersionInDb = articleTranslationVersionService.save(textVersion)
        //todo return dto.
        return articleService.getOneByIdAsDtoWithTranslationsAndVersions(articleInDb.id!!)!!
    }
}
