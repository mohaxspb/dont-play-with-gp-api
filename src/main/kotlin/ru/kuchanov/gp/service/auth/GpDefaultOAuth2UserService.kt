package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import ru.kuchanov.gp.exception.OAuth2AuthenticationProcessingException
import ru.kuchanov.gp.util.user.OAuth2UserInfoFactory

@Service
class GpDefaultOAuth2UserService @Autowired constructor(
    val usersService: GpUserDetailsService
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        println(
            "GpDefaultOAuth2UserService loadUser: " +
                    "${userRequest.additionalParameters}\n" +
                    "${userRequest.clientRegistration}\n" +
                    "${userRequest.accessToken?.tokenValue}"
        )
        val user = super.loadUser(userRequest)
        println("GpDefaultOAuth2UserService user: $user")

        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            userRequest.clientRegistration.registrationId,
            user.attributes
        )
        if (oAuth2UserInfo.getEmail().isEmpty()) {
            throw OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider")
        }

//        println("user.attributes: ${user.attributes.entries}")

        var email = oAuth2UserInfo.getEmail()


        //todo get or register user

        email = "test@test.ru"

        val gpUser = usersService.loadUserByUsername(email)!!
        println("GpDefaultOAuth2UserService gpUser: $gpUser")
        return gpUser
    }
}