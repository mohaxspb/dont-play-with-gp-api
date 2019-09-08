package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.util.OAuth2Utils
import org.springframework.security.oauth2.provider.ClientDetails
import org.springframework.security.oauth2.provider.ClientDetailsService
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.OAuth2Request
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices
import org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
import org.springframework.stereotype.Service
import ru.kuchanov.gp.bean.auth.UserNotFoundException
import java.io.Serializable
import java.util.*
import javax.servlet.http.HttpServletRequest


@Service
class AuthServiceImpl @Autowired constructor(
    val clientDetailsService: ClientDetailsService,
    val userDetailsService: GpUserDetailsService,
    val tokenServices: AuthorizationServerTokenServices
) : AuthService {

    override fun generateAuthenticationForUsernameAndClientId(
        username: String,
        clientId: String
    ): OAuth2Authentication {
        val clientDetails: ClientDetails = clientDetailsService.loadClientByClientId(clientId)

        val requestParameters = mapOf(OAuth2Utils.CLIENT_ID to clientId)
        val authorities: MutableCollection<GrantedAuthority> = clientDetails.authorities
        val approved = true
        val scope: MutableSet<String> = clientDetails.scope
        val resourceIds: MutableSet<String> = clientDetails.resourceIds
        val redirectUri = null
        val responseTypes = setOf<String>()
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
            user.authorities
        )

        return OAuth2Authentication(oAuth2Request, authenticationToken)
    }

    override fun getAccessTokenFromAuthentication(authentication: OAuth2Authentication): OAuth2AccessToken =
        tokenServices.createAccessToken(authentication)

    override fun authenticateRequest(authentication: OAuth2Authentication, request: HttpServletRequest) {
        val sc = SecurityContextHolder.getContext()
        sc.authentication = authentication
        val session = request.getSession(true)
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc)
    }
}
