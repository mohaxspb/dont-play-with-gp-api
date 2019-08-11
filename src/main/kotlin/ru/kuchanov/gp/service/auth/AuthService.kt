package ru.kuchanov.gp.service.auth

import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import javax.servlet.http.HttpServletRequest

interface AuthService {

    fun generateAuthenticationForUsernameAndClientId(username: String, clientId: String): OAuth2Authentication

    fun getAccessTokenFromAuthentication(authentication: OAuth2Authentication): OAuth2AccessToken

    fun authenticateRequest(authentication: OAuth2Authentication, request: HttpServletRequest)
}
