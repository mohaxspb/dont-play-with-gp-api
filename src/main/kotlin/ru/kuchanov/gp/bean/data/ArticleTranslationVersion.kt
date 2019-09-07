package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.gp.model.dto.data.ArticleTranslationVersionDto
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "article_translation_versions")
data class ArticleTranslationVersion(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "article_translation_id")
    val articleTranslationId: Long,

    val text: String,

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

fun ArticleTranslationVersion.toDto(): ArticleTranslationVersionDto =
    ArticleTranslationVersionDto(
        id = id!!,
        articleTranslationId = articleTranslationId,
        text = text,
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


@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "ArticleTranslationVersion not found in db!")
class ArticleTranslationVersionNotFoundException: RuntimeException()

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Version is not approved!")
class VersionNotApprovedException: RuntimeException()

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Version is not published!")
class VersionNotPublishedException: RuntimeException()