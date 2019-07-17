package ru.kuchanov.gp.model.dto

data class UserDto(
    val id: Long,
    val fullName: String?,
    val avatar: String?,
    val score: Long = 0
)