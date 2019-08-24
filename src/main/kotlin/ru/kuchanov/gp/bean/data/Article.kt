package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
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
    val originalLangId: Long,

    @Column(name = "author_id")
    val authorId: Long?,

    val approved: Boolean = false,
    @Column(name = "approver_id")
    val approverId: Long?,
    @Column(name = "approved_date")
    val approvedDate: Timestamp? = null,

    val published: Boolean = false,
    @Column(name = "publisher_id")
    val publisherId: Long? = null,
    @Column(name = "published_date")
    val publishedDate: Timestamp? = null,

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
