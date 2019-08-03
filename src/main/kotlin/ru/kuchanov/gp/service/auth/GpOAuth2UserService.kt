package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import ru.kuchanov.gp.GpConstants.SocialProvider.*
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.UsersAuthorities
import ru.kuchanov.gp.exception.OAuth2AuthenticationProcessingException
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.util.user.OAuth2UserInfoFactory

@Service
class GpOAuth2UserService @Autowired constructor(
    val usersService: GpUserDetailsService,
    val usersAuthoritiesService: UsersAuthoritiesService,
    val passwordGenerator: RandomValueStringGenerator,
    val passwordEncoder: PasswordEncoder,
    val facebookApi: FacebookApi
) : DefaultOAuth2UserService() {

    @Value("\${facebook.clientId}")
    private lateinit var facebookClientId: String
    @Value("\${facebook.clientSecret}")
    private lateinit var facebookClientSecret: String

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        println(
            "GpOAuth2UserService loadUser: " +
                    "${userRequest.additionalParameters}\n" +
                    "${userRequest.clientRegistration}\n" +
                    "${userRequest.accessToken?.tokenValue}"
        )
        val user = super.loadUser(userRequest)
        println("GpOAuth2UserService user: $user")

        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            userRequest.clientRegistration.registrationId,
            user.attributes,
            userRequest.accessToken?.tokenValue
        )

        val provider = oAuth2UserInfo.getProvider()
        val idInProvidersSystem = oAuth2UserInfo.getId()
        val tokenInProvidersSystem = oAuth2UserInfo.providerToken
        val email = oAuth2UserInfo.getEmail()

        if (email.isNullOrEmpty()) {
            //logout from every provider to request email again
            when (provider) {
                GOOGLE -> {
                    //should not happen
                }
                FACEBOOK -> {
                    val facebookLogoutResult =
                        facebookApi
                            .logout(
                                idInProvidersSystem,
                                "$facebookClientId|$facebookClientSecret"
                            )
                            .execute()
                            .body()

                    println("facebookLogoutResult: $facebookLogoutResult")
                }
                VK -> TODO()
                GITHUB -> TODO()
            }
            throw OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider")
        }

//        println("user.attributes: ${user.attributes.entries}")

        val inDbUser = usersService.loadUserByUsername(email)
        if (inDbUser != null) {
            //update users providers ID and token
            inDbUser.apply {
                when (provider) {
                    GOOGLE -> {
                        googleId = idInProvidersSystem
                        googleToken = tokenInProvidersSystem
                    }
                    FACEBOOK -> {
                        facebookId = idInProvidersSystem
                        facebookToken = tokenInProvidersSystem
                    }
                    VK -> {
                        vkId = idInProvidersSystem
                        vkToken = tokenInProvidersSystem
                    }
                    GITHUB -> {
                        githubId = idInProvidersSystem
                        githubToken = tokenInProvidersSystem
                    }
                }
            }
            return usersService.insert(inDbUser)
        } else {
            //check if user registered with this provider ID, but with another email
            val sameUserWithAnotherEmail =
                usersService.getByProviderId(oAuth2UserInfo.getId(), provider)
            if (sameUserWithAnotherEmail != null) {
                //todo handle it somehow. I.e. throw error and let user approve email change.
                return sameUserWithAnotherEmail
            } else {
                //register new user
                val password = passwordGenerator.generate()
                val newUser = usersService.insert(
                    GpUser(
                        fullName = oAuth2UserInfo.getName(),
                        username = email,
                        password = passwordEncoder.encode(password),
                        avatar = oAuth2UserInfo.getImageUrl()
                    )
                        .apply {
                            when (provider) {
                                GOOGLE -> {
                                    googleId = idInProvidersSystem
                                    googleToken = tokenInProvidersSystem
                                }
                                FACEBOOK -> {
                                    facebookId = idInProvidersSystem
                                    facebookToken = tokenInProvidersSystem
                                }
                                VK -> {
                                    vkId = idInProvidersSystem
                                    vkToken = tokenInProvidersSystem
                                }
                                GITHUB -> {
                                    githubId = idInProvidersSystem
                                    githubToken = tokenInProvidersSystem
                                }
                            }
                        }
                )
                println("newUser: $newUser")

                usersAuthoritiesService.insert(UsersAuthorities(userId = newUser.id!!, authority = AuthorityType.USER))

                //todo send email

                return usersService.loadUserByUsername(oAuth2UserInfo.getEmail()!!)!!
            }
        }
    }
}
