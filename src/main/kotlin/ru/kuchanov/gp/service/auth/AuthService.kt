package ru.kuchanov.gp.service.auth

import org.springframework.security.oauth2.common.OAuth2AccessToken

interface AuthService {

    fun getAccessTokenForUsernameAndClientId(username: String, clientId: String): OAuth2AccessToken?
}