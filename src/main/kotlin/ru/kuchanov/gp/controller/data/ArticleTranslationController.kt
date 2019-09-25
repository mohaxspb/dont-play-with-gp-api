package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.GpConstants.ArticleTranslationEndpoint
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.bean.data.*
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.data.*
import java.sql.Timestamp

@RestController
@RequestMapping("/" + ArticleTranslationEndpoint.PATH + "/")
class ArticleTranslationController @Autowired constructor(
    val articleService: ArticleService,
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService,
    val imageService: ImageService,
    val languageService: LanguageService
) {

    @GetMapping
    fun index() =
        "ArticleTranslation endpoint"

    //todo check user. Do not show article if it's not published if user is not admin or author
    @GetMapping(ArticleTranslationEndpoint.Method.FULL + "/{id}")
    fun getByIdFull(@PathVariable(name = "id") id: Long): ArticleTranslationDto =
        articleTranslationService.getOneByIdAsDtoWithVersions(id) ?: throw ArticleTranslationNotFoundException()

    @DeleteMapping(ArticleTranslationEndpoint.Method.DELETE + "/{id}")
    fun deleteById(
        @PathVariable(name = "id") id: Long,
        @AuthenticationPrincipal user: GpUser
    ): Boolean {
        if (user.isAdmin() || articleTranslationService.isUserIsAuthorOfTranslationOrArticleByTranslationId(
                id,
                user.id!!
            )
        ) {
            if (articleTranslationService.countOfTranslationsByTranslationId(id) > 1) {
                return articleTranslationService.deleteById(id)
            } else {
                throw IsTheOnlyTranslationException()
            }
        } else {
            throw GpAccessDeniedException("You are not admin or author of this translation or article!")
        }
    }

    @PostMapping(ArticleTranslationEndpoint.Method.CREATE)
    fun createArticleTranslation(
        @RequestParam(value = "articleId") articleId: Long,
        @RequestParam(value = "imageFile") imageFile: MultipartFile?,
        @RequestParam(value = "imageFileName") imageFileName: String?,
        @RequestParam(value = "langId") langId: Long,
        @RequestParam(value = "title") title: String,
        @RequestParam(value = "shortDescription") shortDescription: String?,
        @RequestParam(value = "text") text: String,
        @AuthenticationPrincipal author: GpUser
    ): ArticleTranslationDto {
        val translationAlreadyExists = articleTranslationService.findAllByArticleId(articleId)
            .find { articleTranslation -> articleTranslation.langId == langId } != null
        if (translationAlreadyExists) {
            throw ArticleTranslationAlreadyException()
        }
        val authorId = author.id!!
        //save image
        val imageUrl = imageFile?.let { imageService.saveImage(authorId, imageFile, imageFileName) }

        //save article translation
        val newTranslation = ArticleTranslation(
            authorId = authorId,
            langId = langId,
            articleId = articleId,
            imageUrl = imageUrl,
            shortDescription = shortDescription,
            title = title
        )
        val savedTranslation = articleTranslationService.save(newTranslation)
        //save article translation version
        val textVersion = ArticleTranslationVersion(
            authorId = authorId,
            articleTranslationId = savedTranslation.id!!,
            text = text
        )
        articleTranslationVersionService.save(textVersion)

        return articleTranslationService.getOneByIdAsDtoWithVersions(savedTranslation.id!!)!!
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

    @PostMapping(ArticleTranslationEndpoint.Method.APPROVE)
    fun approve(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "approve") approve: Boolean
    ): ArticleTranslationDto {
        val articleTranslation = articleTranslationService.getOneById(id)
            ?: throw ArticleTranslationNotFoundException()

        if (user.isAdmin() || articleService.existsByIdAndAuthorId(articleTranslation.articleId, user.id!!)) {
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
        } else {
            throw GpAccessDeniedException("You are not admin or author of article!")
        }
    }

    @PostMapping(ArticleTranslationEndpoint.Method.PUBLISH)
    fun publish(
        @AuthenticationPrincipal user: GpUser,
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "publish") publish: Boolean
    ): ArticleTranslationDto {
        val articleTranslation = articleTranslationService.getOneById(id)
            ?: throw ArticleTranslationNotFoundException()

        if (user.isAdmin() || articleService.existsByIdAndAuthorId(articleTranslation.articleId, user.id!!)) {
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
        } else {
            throw GpAccessDeniedException("You are not admin or author of article!")
        }
    }
}
