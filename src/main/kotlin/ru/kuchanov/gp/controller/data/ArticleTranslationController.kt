package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants.ArticleTranslationEndpoint
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.bean.data.ArticleTranslation
import ru.kuchanov.gp.bean.data.ArticleTranslationNotFoundException
import ru.kuchanov.gp.bean.data.VersionNotApprovedException
import ru.kuchanov.gp.bean.data.VersionNotPublishedException
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService

@RestController
@RequestMapping("/" + ArticleTranslationEndpoint.PATH + "/")
class ArticleTranslationController @Autowired constructor(
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService
) {

    @GetMapping
    fun index() =
        "ArticleTranslation endpoint"

    //todo check user. Do not show article if it's not published if user is not admin or author
    //todo return DTO
    @GetMapping(ArticleTranslationEndpoint.Method.ALL_BY_AUTHOR_ID)
    fun allArticlesByAuthorId(
        @RequestParam(value = "authorId") authorId: Long
    ): List<ArticleTranslation> = TODO()

    //todo check user. Do not show article if it's not published if user is not admin or author
    @GetMapping("{id}")
    fun getById(@PathVariable(name = "id") id: Long): ArticleTranslation =
        articleTranslationService.getOneById(id) ?: throw ArticleTranslationNotFoundException()

    //todo also add user dto for author and approver
    //todo check user. Do not show article if it's not published if user is not admin or author
    @GetMapping("full/{id}")
    fun getByIdFull(@PathVariable(name = "id") id: Long): ArticleTranslationDto =
        articleTranslationService.getOneByIdAsDtoWithVersions(id) ?: throw ArticleTranslationNotFoundException()

    @DeleteMapping("delete/{id}")
    fun deleteById(
        @PathVariable(name = "id") id: Long,
        @AuthenticationPrincipal user: GpUser
    ): Boolean {
        if (user.isAdmin() || articleTranslationService.getOneById(id)!!.authorId == user.id) {
            return articleTranslationService.deleteById(id)
        } else {
            throw GpAccessDeniedException("You are not admin or author of this article!")
        }
    }

    @PostMapping(ArticleTranslationEndpoint.Method.CREATE)
    fun createArticleTranslation(
        @RequestParam(value = "langId") langId: Long,
        @RequestParam(value = "articleId") articleId: Long,
        @RequestParam(value = "title") title: String,
        @RequestParam(value = "shortDescription") shortDescription: String?,
        @RequestParam(value = "text") text: String,
        @AuthenticationPrincipal author: GpUser
    ): ArticleTranslationDto {
        TODO()
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(ArticleTranslationEndpoint.Method.APPROVE)
    fun approve(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "approve") approve: Boolean
    ): ArticleTranslationDto {
        val articleTranslation = articleTranslationService.getOneById(id)
            ?: throw ArticleTranslationNotFoundException()

        if (approve) {
            val versions = articleTranslationVersionService
                .findAllByArticleTranslationId(id)
            val approvedVersions = versions.filter { it.approved }
            if (approvedVersions.isEmpty()) {
                throw VersionNotApprovedException()
            }
        }
        articleTranslation.approved = approve
        articleTranslationService.save(articleTranslation)
        return articleTranslationService.getOneByIdAsDtoWithVersions(id)!!
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(ArticleTranslationEndpoint.Method.PUBLISH)
    fun publish(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "publish") publish: Boolean
    ): ArticleTranslationDto {
        val articleTranslation = articleTranslationService.getOneById(id)
            ?: throw ArticleTranslationNotFoundException()

        if (publish) {
            if (!articleTranslation.approved) {
                throw VersionNotApprovedException()
            }
            val versions = articleTranslationVersionService
                .findAllByArticleTranslationId(id)
            val publishedVersions = versions.filter { it.published }
            if (publishedVersions.isEmpty()) {
                throw VersionNotPublishedException()
            }
        }

        articleTranslation.published = publish
        articleTranslationService.save(articleTranslation)
        return articleTranslationService.getOneByIdAsDtoWithVersions(id)!!
    }
}
