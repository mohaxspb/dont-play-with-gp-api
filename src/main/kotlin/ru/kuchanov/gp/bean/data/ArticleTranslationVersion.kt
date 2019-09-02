package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
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

    val approved: Boolean = false,
    @Column(name = "approver_id")
    val approverId: Long? = null,
    @Column(name = "approved_date")
    val approvedDate: Timestamp? = null,

    val published: Boolean = false,
    @Column(name = "publisher_id")
    val publisherId: Long? = null,
    @Column(name = "published_date")
    val publishedDate: Timestamp? = null,

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
