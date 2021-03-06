package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "articles_tags")
data class ArticlesTags(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "article_id")
    var articleId: Long,
    @Column(name = "tag_id")
    var tagId: Long,

    @Column(name = "author_id")
    val authorId: Long?,

    @field:CreationTimestamp
    val created: Timestamp? = null,
    @field:UpdateTimestamp
    val updated: Timestamp? = null
) : Serializable

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class ArticlesTagsNotFoundException(override val message: String? = "ArticlesTags not found in db!"): RuntimeException(message)
