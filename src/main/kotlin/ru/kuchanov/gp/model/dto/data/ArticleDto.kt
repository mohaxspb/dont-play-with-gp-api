package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.data.Tag
import ru.kuchanov.gp.model.dto.UserDto
import java.sql.Timestamp


data class ArticleDto(
    val id: Long,

    val originalLangId: Long,
    val sourceTitle: String?,
    val sourceUrl: String?,
    val sourceAuthorName: String?,

    val authorId: Long?,

    val approverId: Long? = null,
    val approved: Boolean = false,
    val approvedDate: Timestamp? = null,

    val publisherId: Long? = null,
    val published: Boolean = false,
    val publishedDate: Timestamp? = null,

    val created: Timestamp? = null,
    val updated: Timestamp? = null
) {
    var translations: List<ArticleTranslationDto> = emptyList()
    var tags: List<Tag> = emptyList()
    var author: UserDto? = null
    var approver: UserDto? = null
    var publisher: UserDto? = null
    var fromFuture: Boolean = false
    var commentsCount: Int = 0
}


/**
 * only owned or published.
 */
fun ArticleDto.filteredForUser(user: GpUser): ArticleDto {
    //filter translations only if user is not author of article
    if (user.id!! != authorId) {
        translations = translations.filter { translation ->
            //filter versions only if user is not author of article
            if (user.id != translation.authorId) {
                translation.versions = translation.versions.filter { version ->
                    version.published || version.authorId == user.id
                }
            }
            translation.published || translation.authorId == user.id
        }
    }

    return this
}

fun ArticleDto.isUserAuthorOfSomething(userId: Long): Boolean =
    if (authorId == userId) {
        true
    } else {
        if (translations.firstOrNull { it.authorId == userId } != null) {
            true
        } else {
            val versions = translations.flatMap { it.versions }
            versions.firstOrNull { it.authorId == userId } != null
        }
    }