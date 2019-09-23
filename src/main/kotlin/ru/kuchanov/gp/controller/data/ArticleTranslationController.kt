package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.GpConstants.ArticleTranslationEndpoint
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.bean.data.*
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.data.ArticleTranslationService
import ru.kuchanov.gp.service.data.ArticleTranslationVersionService
import ru.kuchanov.gp.service.data.ImageService
import ru.kuchanov.gp.service.data.LanguageService
import java.sql.Timestamp

@RestController
@RequestMapping("/" + ArticleTranslationEndpoint.PATH + "/")
class ArticleTranslationController @Autowired constructor(
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService,
    val imageService: ImageService,
    val languageService: LanguageService
) {

    @GetMapping
    fun index() =
        "ArticleTranslation endpoint"

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
            throw GpAccessDeniedException("You are not admin or author of this translation!")
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

    @PostMapping(ArticleTranslationEndpoint.Method.EDIT)
    fun editArticleTranslation(
        @RequestParam(value = "translationId") translationId: Long,
        @RequestParam("imageFile") imageFile: MultipartFile?,
        @RequestParam("imageFileName") imageFileName: String?,
        @RequestParam("langId") langId: Long,
        @RequestParam(value = "title") title: String,
        @RequestParam(value = "shortDescription") shortDescription: String?,
        @AuthenticationPrincipal author: GpUser
    ): ArticleTranslationDto {
        languageService.getOneById(langId) ?: throw LanguageNotFoundError()

        //check if user is admin or author of translation
        val translation =
            articleTranslationService.getOneById(translationId) ?: throw ArticleTranslationNotFoundException()
        if (author.isAdmin() || translation.authorId!! == author.id) {
            // save image if need
            var imageUrl: String? = null
            if (imageFile != null && imageFileName != null) {
                imageUrl = imageService.saveImage(author.id!!, imageFile, imageFileName)
            }

            articleTranslationService.save(
                translation.apply {
                    if (imageUrl != null) {
                        this.imageUrl = imageUrl
                    }
                    this.title = title
                    this.shortDescription = shortDescription
                    this.langId = langId
                }
            )

            return articleTranslationService.getOneByIdAsDtoWithVersions(translationId)!!
        } else {
            throw GpAccessDeniedException("You are not admin or author of this translation")
        }
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
        articleTranslation.approverId = user.id!!
        articleTranslation.approvedDate = Timestamp(System.currentTimeMillis())
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
                throw TranslationNotApprovedException()
            }
            val versions = articleTranslationVersionService
                .findAllByArticleTranslationId(id)
            val publishedVersions = versions.filter { it.published }
            if (publishedVersions.isEmpty()) {
                throw VersionNotPublishedException()
            }
        }

        articleTranslation.published = publish
        articleTranslation.publisherId = user.id!!
        articleTranslation.publishedDate = Timestamp(System.currentTimeMillis())
        articleTranslationService.save(articleTranslation)
        return articleTranslationService.getOneByIdAsDtoWithVersions(id)!!
    }
}
