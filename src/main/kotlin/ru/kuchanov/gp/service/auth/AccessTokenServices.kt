package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.UserNotFoundException

@Service
class AccessTokenServices @Autowired constructor(
    val tokenStore: TokenStore,
    val gpUserDetailsService: GpUserDetailsService,
    val gpClientDetailsService: GpClientDetailsService
) : ResourceServerTokenServices {

    override fun loadAuthentication(accessToken: String): OAuth2Authentication =
        tokenStore.readAuthentication(accessToken)

    override fun readAccessToken(accessToken: String): OAuth2AccessToken =
        tokenStore.readAccessToken(accessToken)

    fun deleteAllTokensByUserId(userId: Long) {
        val user = gpUserDetailsService.getById(userId) ?: throw UserNotFoundException()
        val clients = gpClientDetailsService.findAll()
        clients.forEach {
            val accessTokens = tokenStore.findTokensByClientIdAndUserName(
                it.clientId,
                user.username
            )
            accessTokens.forEach { accessToken ->
                val refreshToken = accessToken.refreshToken
                tokenStore.removeRefreshToken(refreshToken)
                tokenStore.removeAccessToken(accessToken)
            }
        }
    }
}
