package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
import org.springframework.stereotype.Service
import ru.kuchanov.gp.repository.auth.UserNotFoundException
import java.io.Serializable
import java.util.*

@Service
class AuthServiceImpl @Autowired constructor(
    val clientDetailsService: ClientDetailsService,
    val userDetailsService: GpUserDetailsService,
    val tokenServices: AuthorizationServerTokenServices
) : AuthService {

    override fun getAccessTokenForUsernameAndClientId(username: String, clientId: String): OAuth2AccessToken? {
        val clientDetails: ClientDetails = clientDetailsService.loadClientByClientId(clientId)

        val requestParameters = mapOf<String, String>()
        val authorities: MutableCollection<GrantedAuthority> = clientDetails.authorities
        val approved = true
        val scope: MutableSet<String> = clientDetails.scope
        val resourceIds: MutableSet<String> = clientDetails.resourceIds
        val redirectUri = null
        val responseTypes = setOf("code")
        val extensionProperties = HashMap<String, Serializable>()

        val oAuth2Request = OAuth2Request(
            requestParameters,
            clientId,
            authorities,
            approved,
            scope,
            resourceIds,
            redirectUri,
            responseTypes,
            extensionProperties
        )

        val user = userDetailsService.loadUserByUsername(username) ?: throw UserNotFoundException()
        val authenticationToken = UsernamePasswordAuthenticationToken(
            user,
            user.password,
            authorities
        )

        val auth = OAuth2Authentication(oAuth2Request, authenticationToken)

        return tokenServices.createAccessToken(auth)
    }
}
