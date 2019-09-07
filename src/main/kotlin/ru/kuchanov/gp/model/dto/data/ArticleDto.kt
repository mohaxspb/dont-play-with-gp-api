package ru.kuchanov.gp.model.dto.data

import ru.kuchanov.gp.bean.auth.GpUser
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
    val approved : Boolean = false,
    val approvedDate: Timestamp? = null,

    val publisherId: Long? = null,
    val published: Boolean = false,
    val publishedDate: Timestamp? = null,

    val created: Timestamp? = null,
    val updated: Timestamp? = null
) {
    var translations: List<ArticleTranslationDto> = emptyList()
    var author: UserDto? = null
    var approver: UserDto? = null
    var publisher: UserDto? = null
}


/**
 * only owned or published.
 */
fun ArticleDto.filteredForUser(user: GpUser): ArticleDto {
    //filter translations
    translations = translations.filter { translation ->
        //filter versions
        translation.versions = translation.versions.filter {
            it.published || it.authorId == user.id
        }
        translation.published || translation.authorId == user.id
    }

    return this
}