package ru.kuchanov.gp.controller.data

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.mail.javamail.JavaMailSender
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
import ru.kuchanov.gp.model.dto.data.isUserAuthorOfSomething
import ru.kuchanov.gp.model.error.GpAccessDeniedException
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.data.*
import java.sql.Timestamp
import java.util.*
import javax.mail.Message
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@RestController
@RequestMapping("/" + GpConstants.ArticleEndpoint.PATH + "/")
class ArticleController @Autowired constructor(
    val articleService: ArticleService,
    val articleTranslationService: ArticleTranslationService,
    val articleTranslationVersionService: ArticleTranslationVersionService,
    val userService: GpUserDetailsService,
    val languageService: LanguageService,
    val imageService: ImageService,
    val tagService: TagService,
    val javaMailSender: JavaMailSender
) {

    @Value("\${my.mail.admin.address}")
    private lateinit var adminEmailAddress: String

    @GetMapping
    fun index() =
        "Article endpoint"

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
            } else if (article.fromFuture) {
                throw ArticleNotAvailableYetException()
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
            } else if (article.fromFuture && !article.isUserAuthorOfSomething(user.id!!)) {
                throw ArticleNotAvailableYetException()
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
        @RequestParam(value = "onlyForCurrentDate", defaultValue = "true") onlyForCurrentDate: Boolean = true,
        @AuthenticationPrincipal user: GpUser?
    ): List<ArticleDto> {
        if ((published && approved && onlyForCurrentDate) || user?.isAdmin() == true) {
            return articleService.getPublishedArticles(
                offset,
                limit,
                published,
                approved,
                withTranslations,
                withVersions,
                onlyForCurrentDate
            )
        } else {
            throw GpAccessDeniedException("Only admins can see not published or approved articles or articles from future!")
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
        tagService.saveTagsForArticle(tags, articleInDb.id!!, authorId)

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

        val createdArticle = articleService.getOneByIdAsDtoWithTranslationsAndVersions(articleInDb.id!!)!!

        javaMailSender.send { mimeMessage: MimeMessage ->
            mimeMessage.setFrom()
            mimeMessage.setRecipient(Message.RecipientType.TO, InternetAddress(adminEmailAddress))
            mimeMessage.subject = "New article created!"
            mimeMessage.setText("New article created! ${createdArticle.translations[0].title} by ${createdArticle.author!!.fullName}")
        }

        //return dto.
        return createdArticle
    }

    @PostMapping(GpConstants.ArticleEndpoint.Method.EDIT)
    fun editArticle(
        @RequestParam(value = "articleId") articleId: Long,
        @RequestParam(value = "langId") langId: Long,
        @RequestParam(value = "sourceUrl") sourceUrl: String?,
        @RequestParam(value = "sourceAuthorName") sourceAuthorName: String?,
        @RequestParam(value = "sourceTitle") sourceTitle: String?,
        @RequestParam(value = "tags") tags: List<String>,
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

            //update tags, by deleting all for article and saving new ones
            tagService.deleteAllByArticleId(articleId)
            tagService.saveTagsForArticle(tags, articleId, author.id!!)

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
        } else {
            if (article.published) {
                throw ArticleIsPublishedException("You can't disapprove published article! Unpublish it first.")
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
        val userId = user.id!!
        val timestamp = Timestamp(System.currentTimeMillis())

        if (publish) {
            checkArticle(article, userId)
        }

        article.published = publish
        article.publisherId = userId
        article.publishedDate = timestamp
        articleService.save(article)
        return articleService.getOneByIdAsDtoWithTranslationsAndVersions(id)!!
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping(GpConstants.ArticleEndpoint.Method.PUBLISH_WITH_DATE)
    fun publishWithDate(
        @RequestParam(name = "id") id: Long,
        @RequestParam(name = "publishDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) publishDate: Date,
        @AuthenticationPrincipal user: GpUser
    ): ArticleDto {
        val article = articleService.getOneById(id) ?: throw ArticleNotFoundException()
        val userId = user.id!!

        checkArticle(article, userId)

        article.published = true
        article.publisherId = userId

        article.publishedDate = Timestamp(publishDate.time)
        articleService.save(article)
        return articleService.getOneByIdAsDtoWithTranslationsAndVersions(id)!!
    }

    private fun checkArticle(article: Article, userId: Long) {
        val articleId = article.id!!
        //allow publish if there is only one translation and one text version for this article
        val countOfTranslations = articleTranslationService.countTranslationsByArticleId(articleId)
        val countOfVersions = articleTranslationVersionService.countByArticleId(articleId)
        if (countOfTranslations == 1 && countOfVersions == 1) {
            val timestamp = Timestamp(System.currentTimeMillis())

            article.approved = true
            article.approverId = userId
            article.approvedDate = timestamp
            articleService.save(article)
            //approve and publish all translations and its versions
            val translation = articleTranslationService.findAllByArticleId(articleId)[0]
            translation.approved = true
            translation.approverId = userId
            translation.approvedDate = timestamp
            translation.published = true
            translation.publisherId = userId
            translation.publishedDate = timestamp
            articleTranslationService.save(translation)

            val version = articleTranslationVersionService.findAllByArticleTranslationId(translation.id!!)[0]
            version.approved = true
            version.approverId = userId
            version.approvedDate = timestamp
            version.published = true
            version.publisherId = userId
            version.publishedDate = timestamp
            articleTranslationVersionService.save(version)
        } else {
            if (!article.approved) {
                throw ArticleNotApprovedException()
            }
            val translations = articleTranslationService.findAllByArticleId(articleId)
            val publishedTranslations = translations.filter { it.published }
            if (publishedTranslations.isEmpty()) {
                throw TranslationNotPublishedException()
            }
        }
    }
}
