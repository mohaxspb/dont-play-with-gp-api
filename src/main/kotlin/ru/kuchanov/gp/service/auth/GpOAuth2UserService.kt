package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.GpConstants.SocialProvider.*
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.UsersAuthorities
import ru.kuchanov.gp.exception.OAuth2AuthenticationProcessingException
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.network.GitHubApi
import ru.kuchanov.gp.util.user.OAuth2UserInfoFactory

@Service
class GpOAuth2UserService @Autowired constructor(
    val usersService: GpUserDetailsService,
    val usersAuthoritiesService: UsersAuthoritiesService,
    val passwordGenerator: RandomValueStringGenerator,
    val passwordEncoder: PasswordEncoder,
    val facebookApi: FacebookApi,
    val githubApi: GitHubApi
) : DefaultOAuth2UserService() {

    @Value("\${spring.security.oauth2.client.registration.facebook.clientId}")
    private lateinit var facebookClientId: String
    @Value("\${spring.security.oauth2.client.registration.facebook.clientSecret}")
    private lateinit var facebookClientSecret: String

    @Value("\${spring.security.oauth2.client.registration.github.clientId}")
    private lateinit var githubClientId: String
    @Value("\${spring.security.oauth2.client.registration.github.clientSecret}")
    private lateinit var githubClientSecret: String

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        println("GpOAuth2UserService loadUser: ${userRequest.additionalParameters}")
        println("GpOAuth2UserService loadUser: ${userRequest.accessToken?.tokenValue}")
        println("GpOAuth2UserService loadUser: ${userRequest.clientRegistration.providerDetails.userInfoEndpoint.userNameAttributeName}")
        println("GpOAuth2UserService loadUser: ${userRequest.clientRegistration.providerDetails.userInfoEndpoint.uri}")

        val user = super.loadUser(userRequest)
        println("GpOAuth2UserService user: $user")
        val userAttributes = user.attributes as Map<String, Any?>
        println("GpOAuth2UserService user: ${userAttributes.entries}")

        val additionalParams: Map<String, Any?>
        if (userRequest.clientRegistration.registrationId == GpConstants.SocialProvider.VK.name.toLowerCase()) {
            val response = userAttributes["response"]
            println("GpOAuth2UserService response: ${response?.javaClass?.name}")
            val responseTyped = response as? List<LinkedHashMap<String, Any?>?>
            println("GpOAuth2UserService response: ${responseTyped?.get(0)?.javaClass}")

            additionalParams = LinkedHashMap(userRequest.additionalParameters)
            val vkParamsMap = responseTyped?.get(0)
            val name = "${vkParamsMap?.get("first_name")} ${vkParamsMap?.get("last_name")}"
            additionalParams["name"] = name
            additionalParams["picture"] = vkParamsMap?.get("photo_max")
        } else {
            additionalParams = user.attributes
        }

        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            userRequest.clientRegistration.registrationId,
            additionalParams,
            userRequest.accessToken?.tokenValue
        )
        println("oAuth2UserInfo: $oAuth2UserInfo")

        val provider = oAuth2UserInfo.getProvider()
        val idInProvidersSystem = oAuth2UserInfo.getId()
        val tokenInProvidersSystem = oAuth2UserInfo.providerToken
        val email = oAuth2UserInfo.getEmail()
        val image = oAuth2UserInfo.getImageUrl()

        if (email.isNullOrEmpty()) {
            //logout from every provider to request email again
            when (provider) {
                GOOGLE -> {
                    //should not happen
                }
                VK -> {
                    //nothing to do
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
                GITHUB -> {
                    val authorization =
                        "Basic " + String(Base64Utils.encode("$githubClientId:$githubClientSecret".toByteArray()))
                    val githubLogoutResult = githubApi.logout(
                        authorization,
                        githubClientId,
                        tokenInProvidersSystem!!
                    )
                        .execute()

                    println("githubLogoutResult: $githubLogoutResult")
                    val message =
                        """Email not found in GitHub response. You can add it at GitHub profile (https://github.com/settings/profile). See "Public email" option."""
                    throw OAuth2AuthenticationProcessingException(message)
                }
            }
            throw OAuth2AuthenticationProcessingException("Email not found. Add it to your profile and enable access to it while login.")
        }

//        println("user.attributes: ${user.attributes.entries}")

        val inDbUser = usersService.loadUserByUsername(email)
        if (inDbUser != null) {
            //update users providers ID and token and image
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
                image?.let { avatar = it }
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
