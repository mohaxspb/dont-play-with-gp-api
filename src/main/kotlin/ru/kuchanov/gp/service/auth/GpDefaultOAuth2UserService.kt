package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User

@Service
class GpDefaultOAuth2UserService @Autowired constructor(
    val authenticationManager: AuthenticationManager
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        println(
            "GpDefaultOAuth2UserService loadUser: " +
                    "${userRequest?.additionalParameters}\n" +
                    "${userRequest?.clientRegistration}\n" +
                    "${userRequest?.accessToken?.tokenValue}"
        )
        val authentication = tokenExtractor.extract(userRequest)
        val authResult = authenticationManager.authenticate(authentication)
        SecurityContextHolder.getContext().authentication = authResult

        return super.loadUser(userRequest)
    }
}