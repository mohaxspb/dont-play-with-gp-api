package ru.kuchanov.gp.bean.data

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import ru.kuchanov.gp.model.dto.data.CommentDto
import java.io.Serializable
import java.sql.Timestamp
import javax.persistence.*

@Entity
@Table(name = "comments")
data class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    var text: String,

    @Column(name = "article_id")
    val articleId: Long,

    @Column(name = "author_id")
    val authorId: Long,

    @field:CreationTimestamp
    val created: Timestamp? = null,
    @field:UpdateTimestamp
    val updated: Timestamp? = null
) : Serializable

fun Comment.toDto() = CommentDto(
    id = id!!,
    text = text,
    articleId = articleId,
    authorId = authorId,
    created = created,
    updated = updated
)

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class CommentNotFoundException(override val message: String? = "Comment not found in db!") : RuntimeException(message)