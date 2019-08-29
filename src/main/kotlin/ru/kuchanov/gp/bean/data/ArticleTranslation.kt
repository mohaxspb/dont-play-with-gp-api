package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
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
    val langId: Long,
    @Column(name = "article_id")
    val articleId: Long,

    val title: String,
    @Column(name = "short_description")
    val shortDescription: String?,
    @Column(name = "image_url")
    val imageUrl: String?,

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
