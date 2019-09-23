package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.bean.data.*
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import ru.kuchanov.gp.model.dto.data.PublishVersionResultDto
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService
import java.sql.Timestamp

@RestController
@RequestMapping("/" + GpConstants.ArticleTranslationVersionEndpoint.PATH + "/")
class ArticleTranslationVersionController @Autowired constructor(
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService
) {

    @GetMapping
    fun index() =
        "ArticleTranslationVersion endpoint"

    //todo check user. Do not show article if it's not published if user is not admin or author
    @GetMapping("{id}")
    fun getById(@PathVariable(name = "id") id: Long): ArticleTranslationVersion =
        articleTranslationVersionService.getOneById(id) ?: throw ArticleTranslationVersionNotFoundException()

    @DeleteMapping("delete/{id}")
    fun deleteById(
        @PathVariable(name = "id") id: Long,
        @AuthenticationPrincipal user: GpUser
    ): Boolean {
        if (user.isAdmin() || articleTranslationVersionService.getOneById(id)!!.authorId == user.id) {
            return articleTranslationVersionService.deleteById(id)
        } else {
            throw GpAccessDeniedException("You are not admin or author of this version!")
        }
    }

    @PostMapping(GpConstants.ArticleTranslationVersionEndpoint.Method.CREATE)
    fun createArticleTranslationVersion(
        @RequestParam(value = "articleTranslationId") articleTranslationId: Long,
        @RequestParam(value = "text") text: String,
        @AuthenticationPrincipal author: GpUser
    ): ArticleTranslationVersionDto {
        //check if user is admin or author of translation or translation is published
        val translation = articleTranslationService.getOneById(articleTranslationId)
            ?: throw ArticleTranslationNotFoundException()

        if(author.isAdmin() || translation.published || author.id!! == translation.id!!){
            val newVersion = ArticleTranslationVersion(
                articleTranslationId = articleTranslationId,
                authorId = author.id!!,
                text = text
            )
            val createdVersion = articleTranslationVersionService.save(newVersion)
            return articleTranslationVersionService.getOneByIdAsDto(createdVersion.id!!)!!
        } else{
            throw GpAccessDeniedException("You are not author or this translation, or translation is not published or you are not admin!")
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(GpConstants.ArticleTranslationVersionEndpoint.Method.APPROVE)
    fun approve(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "approve") approve: Boolean
    ): ArticleTranslationVersionDto {
        val articleTranslationVersion = articleTranslationVersionService.getOneById(id)
            ?: throw ArticleTranslationVersionNotFoundException()

        articleTranslationVersion.approved = approve
        articleTranslationVersion.approverId = user.id!!
        articleTranslationVersion.approvedDate = Timestamp(System.currentTimeMillis())
        articleTranslationVersionService.save(articleTranslationVersion)
        return articleTranslationVersionService.getOneByIdAsDto(id)!!
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(GpConstants.ArticleTranslationVersionEndpoint.Method.PUBLISH)
    fun publish(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "publish") publish: Boolean
    ): PublishVersionResultDto {
        val articleTranslationVersion = articleTranslationVersionService.getOneById(id)
            ?: throw ArticleTranslationVersionNotFoundException()

        var alreadyPublishedVersion: ArticleTranslationVersion? = null

        if (publish) {
            if (!articleTranslationVersion.approved) {
                throw VersionNotApprovedException()
            }

            //check if there is already published versions and unpublish it
            alreadyPublishedVersion = articleTranslationVersionService
                .getPublishedByTranslationId(articleTranslationVersion.articleTranslationId)

            if (alreadyPublishedVersion != null) {
                alreadyPublishedVersion.published = false
                alreadyPublishedVersion.publisherId = user.id!!
                alreadyPublishedVersion.publishedDate = Timestamp(System.currentTimeMillis())
                articleTranslationVersionService.save(alreadyPublishedVersion)
            }
        }

        articleTranslationVersion.published = publish
        articleTranslationVersion.publisherId = user.id!!
        articleTranslationVersion.publishedDate = Timestamp(System.currentTimeMillis())
        articleTranslationVersionService.save(articleTranslationVersion)
        val updatedVersion = articleTranslationVersionService.getOneByIdAsDto(id)!!
        val unpublishedVersion =
            alreadyPublishedVersion?.id?.let { articleTranslationVersionService.getOneByIdAsDto(it) }

        return PublishVersionResultDto(updatedVersion = updatedVersion, unpublishedVersion = unpublishedVersion)
    }
}
