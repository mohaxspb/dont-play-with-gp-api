package ru.kuchanov.gp.model.dto


data class UserDto(
    val id: Long,
    val fullName: String?,
    val avatar: String?,
    val primaryLanguageId: Long
) {
    var authorities = listOf<AuthorityDto>()
}
