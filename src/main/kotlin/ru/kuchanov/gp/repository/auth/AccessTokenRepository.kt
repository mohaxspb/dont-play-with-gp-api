package ru.kuchanov.gp.repository.auth

import org.springframework.data.jpa.repository.JpaRepository
import ru.kuchanov.gp.bean.auth.OAuthAccessToken

interface AccessTokenRepository : JpaRepository<OAuthAccessToken, String>