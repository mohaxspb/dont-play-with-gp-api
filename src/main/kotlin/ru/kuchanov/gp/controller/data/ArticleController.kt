package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.bean.data.*
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.filteredForUser
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.data.ArticleService
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService
import java.sql.Timestamp

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

    //todo check user. Do not show article if it's not published if user is not admin or author
    //todo return DTO
    @GetMapping(GpConstants.ArticleEndpoint.Method.ALL_BY_AUTHOR_ID)
    fun allArticlesByAuthorId(
        @RequestParam(value = "authorId") authorId: Long
    ): List<Article> = articleService.findAllByAuthorId(authorId)

    //todo check user. Do not show article if it's not published if user is not admin or author
    @GetMapping("{id}")
    fun getById(
        @PathVariable(name = "id") id: Long
    ): Article =
        articleService.getOneById(id) ?: throw ArticleNotFoundException()

    @GetMapping("full/{id}")
    fun getByIdFull(
        @PathVariable(name = "id") id: Long,
        @AuthenticationPrincipal user: GpUser?
    ): ArticleDto {
        val article = articleService.getOneByIdAsDtoWithTranslationsAndVersions(id)
            ?: throw ArticleNotFoundException()

        if (user == null) {
            if (!article.published) {
                throw ArticleNotPublishedException()
            } else {
                return article.apply {
                    translations = translations.filter { translation ->
                        translation.versions = translation.versions.filter { it.published }
                        return@filter translation.published
                    }
                }
            }
        } else {
            return if (user.isAdmin()) {
                article
            } else {
                article.filteredForUser(user)
            }
        }
    }

    @DeleteMapping("delete/{id}")
    fun deleteById(
        @PathVariable(name = "id") id: Long,
        @AuthenticationPrincipal user: GpUser
    ): Boolean {
        if (user.isAdmin() || articleService.getOneById(id)!!.authorId == user.id) {
            return articleService.deleteById(id)
        } else {
            throw GpAccessDeniedException("You are not admin or author of this article!")
        }
    }

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
        articleTranslationVersionService.save(textVersion)
        //return dto.
        return articleService.getOneByIdAsDtoWithTranslationsAndVersions(articleInDb.id!!)!!
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(GpConstants.ArticleEndpoint.Method.APPROVE)
    fun approve(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "approve") approve: Boolean
    ): ArticleDto {
        val article = articleService.getOneById(id) ?: throw ArticleNotFoundException()

        if (approve) {
            val translations = articleTranslationService
                .findAllByArticleId(id)
            val approvedTranslations = translations.filter { it.approved }
            if (approvedTranslations.isEmpty()) {
                throw TranslationNotApprovedException()
            }
        }
        article.approved = approve
        article.approverId = user.id!!
        article.approvedDate = Timestamp(System.currentTimeMillis())
        articleService.save(article)
        return articleService.getOneByIdAsDtoWithTranslationsAndVersions(id)!!
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(GpConstants.ArticleEndpoint.Method.PUBLISH)
    fun publish(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "publish") publish: Boolean
    ): ArticleDto {
        val article = articleService.getOneById(id) ?: throw ArticleNotFoundException()

        if (publish) {
            if (!article.approved) {
                throw ArticleNotApprovedException()
            }
            val translations = articleTranslationService
                .findAllByArticleId(id)
            val publishedTranslations = translations.filter { it.published }
            if (publishedTranslations.isEmpty()) {
                throw TranslationNotPublishedException()
            }
        }

        article.published = publish
        article.publisherId = user.id!!
        article.publishedDate = Timestamp(System.currentTimeMillis())
        articleService.save(article)
        return articleService.getOneByIdAsDtoWithTranslationsAndVersions(id)!!
    }
}
