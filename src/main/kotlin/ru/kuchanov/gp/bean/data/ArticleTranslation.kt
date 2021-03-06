package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.gp.model.dto.data.ArticleTranslationDto
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_translations")
data class ArticleTranslation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "lang_id")
    var langId: Long,
    @Column(name = "article_id")
    val articleId: Long,

    var title: String,
    @Column(name = "short_description")
    var shortDescription: String?,
    @Column(name = "image_url")
    var imageUrl: String?,

    @Column(name = "author_id")
    val authorId: Long?,

    var approved: Boolean = false,
    @Column(name = "approver_id")
    var approverId: Long? = null,
    @Column(name = "approved_date")
    var approvedDate: Timestamp? = null,

    var published: Boolean = false,
    @Column(name = "publisher_id")
    var publisherId: Long? = null,
    @Column(name = "published_date")
    var publishedDate: Timestamp? = null,

    @field:CreationTimestamp
    val created: Timestamp? = null,
    @field:UpdateTimestamp
    val updated: Timestamp? = null
) : Serializable

fun ArticleTranslation.toDto(): ArticleTranslationDto =
    ArticleTranslationDto(
        id = id!!,
        langId = langId,
        articleId = articleId,
        title = title,
        shortDescription = shortDescription,
        imageUrl = imageUrl,
        authorId = authorId,
        approverId = approverId,
        approved = approved,
        approvedDate = approvedDate,
        publisherId = publisherId,
        published = published,
        publishedDate = publishedDate,
        created = created,
        updated = updated
    )

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ArticleTranslationNotFoundException(
    override val message: String? = "ArticleTranslation not found in db!"
) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.CONFLICT)
class ArticleTranslationAlreadyException(
    override val message: String? = "ArticleTranslation with this language already exists in db!"
) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "ArticleTranslation is not approved!")
class TranslationNotApprovedException : RuntimeException()

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "ArticleTranslation is not published!")
class TranslationNotPublishedException : RuntimeException()

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
class TranslationIsPublishedException(
    override val message: String? = "ArticleTranslation is published!"
) : RuntimeException(message)

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED)
class IsTheOnlyTranslationException(
    override val message: String? = "You are not allowed to delete the only translation in article"
) : RuntimeException(message)
