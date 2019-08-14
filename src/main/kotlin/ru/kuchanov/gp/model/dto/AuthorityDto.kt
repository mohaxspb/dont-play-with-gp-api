package ru.kuchanov.gp.model.dto

import ru.kuchanov.gp.bean.auth.AuthorityType
import javax.persistence.EnumType
import javax.persistence.Enumerated


data class AuthorityDto(
    @Enumerated(EnumType.STRING)
    val authority: AuthorityType
)
