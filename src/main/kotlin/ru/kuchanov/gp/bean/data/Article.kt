package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.gp.model.dto.data.ArticleDto
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "articles")
data class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "original_lang_id")
    var originalLangId: Long,

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

    /**
     * source* is null, if this is original article for dont-play-with-gp. Else - data from other site
     */
    @Column(name = "source_title")
    var sourceTitle: String? = null,
    /**
     * source* is null, if this is original article for dont-play-with-gp. Else - data from other site
     */
    @Column(name = "source_url")
    var sourceUrl: String? = null,
    /**
     * source* is null, if this is original article for dont-play-with-gp. Else - data from other site
     */
    @Column(name = "source_author_name")
    var sourceAuthorName: String? = null,

    @field:CreationTimestamp
    val created: Timestamp? = null,
    @field:UpdateTimestamp
    val updated: Timestamp? = null
) : Serializable

fun Article.toDto(): ArticleDto =
    ArticleDto(
        id = id!!,
        originalLangId = originalLangId,
        sourceTitle = sourceTitle,
        sourceAuthorName = sourceAuthorName,
        sourceUrl = sourceUrl,
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
class ArticleNotFoundException(override val message: String? = "Article not found in db!"): RuntimeException(message)

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Article is not published!")
class ArticleNotPublishedException: RuntimeException()

@ResponseStatus(value = HttpStatus.PRECONDITION_FAILED, reason = "Article is not approved!")
class ArticleNotApprovedException: RuntimeException()