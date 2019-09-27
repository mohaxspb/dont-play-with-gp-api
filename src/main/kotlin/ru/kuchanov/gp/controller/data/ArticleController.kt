package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.UserNotFoundException
import ru.kuchanov.gp.bean.auth.isAdmin
import ru.kuchanov.gp.bean.data.*
import ru.kuchanov.gp.model.dto.data.ArticleDto
import ru.kuchanov.gp.model.dto.data.filteredForUser
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.data.*
import java.sql.Timestamp

@RestController
@RequestMapping("/" + GpConstants.ArticleEndpoint.PATH + "/")
class ArticleController @Autowired constructor(
    val articleService: ArticleService,
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService,
    val userService: GpUserDetailsService,
    val languageService: LanguageService,
    val imageService: ImageService,
    val tagService: TagService
) {

    @GetMapping
    fun index() =
        "Article endpoint"

    //todo check user. Do not show article if it's not published if user is not admin or author
    @GetMapping("{id}")
    fun getById(
        @PathVariable(name = "id") id: Long
    ): Article =
        articleService.getOneById(id) ?: throw ArticleNotFoundException()

    @GetMapping(GpConstants.ArticleEndpoint.Method.FULL + "/{id}")
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

    @GetMapping(GpConstants.ArticleEndpoint.Method.ALL)
    fun getArticles(
        @RequestParam(value = "limit") limit: Int,
        @RequestParam(value = "offset") offset: Int,
        @RequestParam(value = "published", defaultValue = "true") published: Boolean = true,
        @RequestParam(value = "approved", defaultValue = "true") approved: Boolean = true,
        @RequestParam(value = "withTranslations", defaultValue = "true") withTranslations: Boolean = true,
        @RequestParam(value = "withVersions", defaultValue = "false") withVersions: Boolean = false,
        @AuthenticationPrincipal user: GpUser?
    ): List<ArticleDto> {
        if ((published && approved) || user?.isAdmin() == true) {
            return articleService.getPublishedArticles(
                offset,
                limit,
                published,
                approved,
                withTranslations,
                withVersions
            )
        } else {
            throw GpAccessDeniedException("Only admins can see not published or approved articles!")
        }
    }

    @GetMapping(GpConstants.ArticleEndpoint.Method.ALL_BY_AUTHOR_ID)
    fun allArticlesByAuthorId(
        @RequestParam(value = "authorId") authorId: Long,
        @AuthenticationPrincipal user: GpUser?
    ): List<ArticleDto> {
        userService.getById(authorId) ?: throw UserNotFoundException()
        return if (user != null && (user.isAdmin() || user.id == authorId)) {
            // all
            articleService.findAllByAuthorIdWithTranslationsAsDto(authorId, false)
        } else {
            // only published
            articleService.findAllByAuthorIdWithTranslationsAsDto(authorId)
        }
    }

    @DeleteMapping("delete/{id}")
    fun deleteById(
        @PathVariable(name = "id") id: Long,
        @AuthenticationPrincipal user: GpUser
    ): Boolean {
        if (user.isAdmin() || articleService.existsByIdAndAuthorId(id, user.id!!)) {
            if (articleService.getOneById(id) != null) {
                return articleService.deleteById(id)
            } else {
                throw ArticleNotFoundException()
            }
        } else {
            throw GpAccessDeniedException("You are not admin or author of this article!")
        }
    }

    @PostMapping(GpConstants.ArticleEndpoint.Method.CREATE)
    fun createArticle(
        @RequestParam("image") image: MultipartFile?,
        @RequestParam("imageName") imageName: String?,
        @RequestParam(value = "sourceTitle") sourceTitle: String?,
        @RequestParam(value = "sourceAuthorName") sourceAuthorName: String?,
        @RequestParam(value = "sourceUrl") sourceUrl: String?,
        @RequestParam(value = "tags") tags: List<String>,
        @RequestParam(value = "articleLanguageId") articleLanguageId: Long,
        @RequestParam(value = "title") title: String,
        @RequestParam(value = "shortDescription") shortDescription: String?,
        @RequestParam(value = "text") text: String,
        @AuthenticationPrincipal author: GpUser
    ): ArticleDto {
        val authorId = author.id!!
        //save image
        val imageUrl = image?.let { imageService.saveImage(authorId, image, imageName) }

        //save tags
        val tagsForArticle = mutableListOf<Tag>()
        tags.forEach {
            val tagInDb = tagService.findByTitle(it)
            if (tagInDb == null) {
                tagsForArticle.add(tagService.save(Tag(title = it, authorId = authorId)))
            } else {
                tagsForArticle.add(tagInDb)
            }
        }

        //save article
        val article = Article(
            authorId = authorId,
            originalLangId = articleLanguageId,
            sourceTitle = sourceTitle,
            sourceAuthorName = sourceAuthorName,
            sourceUrl = sourceUrl
        )
        val articleInDb = articleService.save(article)

        //save tagsForArticle
        tagService.saveTagsForArticle(tagsForArticle, articleInDb.id!!, authorId)

        //save article translation
        val articleTranslation = ArticleTranslation(
            authorId = authorId,
            langId = articleLanguageId,
            articleId = articleInDb.id!!,
            imageUrl = imageUrl,
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

    @PostMapping(GpConstants.ArticleEndpoint.Method.EDIT)
    fun editArticle(
        @RequestParam(value = "articleId") articleId: Long,
        @RequestParam(value = "langId") langId: Long,
        @RequestParam(value = "sourceUrl") sourceUrl: String?,
        @RequestParam(value = "sourceAuthorName") sourceAuthorName: String?,
        @RequestParam(value = "sourceTitle") sourceTitle: String?,
        @AuthenticationPrincipal author: GpUser
    ): ArticleDto {
        val language = languageService.getOneById(langId) ?: throw LanguageNotFoundError()

        val articleToUpdate = articleService.getOneById(articleId) ?: throw ArticleNotFoundException()

        //check if article has translation with given lang
        if (!articleTranslationService.findAllByArticleId(articleId).map { it.langId }.contains(langId)) {
            throw ArticleTranslationNotFoundException("Article doesn't have translation with ${language.langName} language")
        }

        if (author.isAdmin() || articleToUpdate.authorId == author.id) {
            articleToUpdate.originalLangId = langId
            articleToUpdate.sourceUrl = sourceUrl
            articleToUpdate.sourceAuthorName = sourceAuthorName
            articleToUpdate.sourceTitle = sourceTitle
            articleService.save(articleToUpdate)

            return articleService.getOneByIdAsDtoWithTranslationsAndVersions(articleId)!!
        } else {
            throw GpAccessDeniedException("You are not admin or author of this article!")
        }
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
