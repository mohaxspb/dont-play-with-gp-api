package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
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
import ru.kuchanov.gp.service.mail.MailService
import java.sql.Timestamp

@RestController
@RequestMapping("/" + GpConstants.ArticleTranslationVersionEndpoint.PATH + "/")
class ArticleTranslationVersionController @Autowired constructor(
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService,
    val mailService: MailService
) {

    @GetMapping
    fun index() =
        "ArticleTranslationVersion endpoint"

    @DeleteMapping("delete/{id}")
    fun deleteById(
        @PathVariable(name = "id") id: Long,
        @AuthenticationPrincipal user: GpUser
    ): Boolean {
        //check if user is admin or author of version, translation or article
        if (user.isAdmin() ||
            articleTranslationVersionService.isUserIsAuthorOfVersionOrTranslationOrArticleByVersionId(id, user.id!!)
        ) {
            // also, if it's the only version do not allow to delete!
            if (articleTranslationVersionService.countOfVersionsByVersionId(id) > 1) {
                return articleTranslationVersionService.deleteById(id)
            } else {
                throw IsTheOnlyVersionException()
            }
        } else {
            throw GpAccessDeniedException("You are not admin or author of this translation!")
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

        if (author.isAdmin() || translation.published || author.id!! == translation.id!!) {
            val newVersion = ArticleTranslationVersion(
                articleTranslationId = articleTranslationId,
                authorId = author.id!!,
                text = text
            )
            val createdVersionId = articleTranslationVersionService.save(newVersion).id!!
            val createdVersion = articleTranslationVersionService.getOneByIdAsDto(createdVersionId)!!

            mailService.sendVersionCreatedMail(createdVersion)

            return createdVersion
        } else {
            throw GpAccessDeniedException("You are not author or this translation, or translation is not published or you are not admin!")
        }
    }

    @PostMapping(GpConstants.ArticleTranslationVersionEndpoint.Method.EDIT)
    fun editArticleTranslationVersion(
        @RequestParam(value = "versionId") versionId: Long,
        @RequestParam(value = "text") text: String,
        @AuthenticationPrincipal author: GpUser
    ): ArticleTranslationVersionDto {
        val versionToEdit = articleTranslationVersionService.getOneById(versionId)
            ?: throw ArticleTranslationVersionNotFoundException()

        //check if user is admin or author of version, translation or article
        if (author.isAdmin()
            || articleTranslationVersionService.isUserIsAuthorOfVersionOrTranslationOrArticleByVersionId(
                versionId,
                author.id!!
            )
        ) {
            versionToEdit.text = text
            articleTranslationVersionService.save(versionToEdit)
            return articleTranslationVersionService.getOneByIdAsDto(versionId)!!
        } else {
            throw GpAccessDeniedException("You are not author or this version or you are not admin!")
        }
    }

    @PostMapping(GpConstants.ArticleTranslationVersionEndpoint.Method.APPROVE)
    fun approve(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "approve") approve: Boolean
    ): ArticleTranslationVersionDto {
        val articleTranslationVersion = articleTranslationVersionService.getOneById(id)
            ?: throw ArticleTranslationVersionNotFoundException()

        if (user.isAdmin()
            || articleTranslationService.isUserIsAuthorOfTranslationOrArticleByTranslationId(
                articleTranslationVersion.articleTranslationId,
                user.id!!
            )
        ) {
            if (articleTranslationVersion.published) {
                throw VersionIsPublishedException("You can't disapprove published version! Unpublish it first.")
            }
            articleTranslationVersion.approved = approve
            articleTranslationVersion.approverId = user.id!!
            articleTranslationVersion.approvedDate = Timestamp(System.currentTimeMillis())
            articleTranslationVersionService.save(articleTranslationVersion)

            val updatedVersion = articleTranslationVersionService.getOneByIdAsDto(id)!!
            mailService.sendVersionApprovedMail(updatedVersion)
            return updatedVersion
        } else {
            throw GpAccessDeniedException("You are not admin or author of this translation or article!")
        }
    }

    @PostMapping(GpConstants.ArticleTranslationVersionEndpoint.Method.PUBLISH)
    fun publish(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "publish") publish: Boolean
    ): PublishVersionResultDto {
        val articleTranslationVersion = articleTranslationVersionService.getOneById(id)
            ?: throw ArticleTranslationVersionNotFoundException()

        if (user.isAdmin()
            || articleTranslationService.isUserIsAuthorOfTranslationOrArticleByTranslationId(
                articleTranslationVersion.articleTranslationId,
                user.id!!
            )
        ) {
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

            mailService.sendVersionPublishedMail(updatedVersion)

            return PublishVersionResultDto(updatedVersion = updatedVersion, unpublishedVersion = unpublishedVersion)
        } else {
            throw GpAccessDeniedException("You are not admin or author of this translation or article!")
        }
    }
}
