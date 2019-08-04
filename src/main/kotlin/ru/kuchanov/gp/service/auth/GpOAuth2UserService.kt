package ru.kuchanov.gp.service.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator
import org.springframework.security.oauth2.core.AuthenticationMethod
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.GpConstants.SocialProvider.*
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.bean.auth.UsersAuthorities
import ru.kuchanov.gp.exception.OAuth2AuthenticationProcessingException
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.network.GitHubApi
import ru.kuchanov.gp.util.user.OAuth2UserInfoFactory
import java.net.URI

@Service
class GpOAuth2UserService @Autowired constructor(
    val usersService: GpUserDetailsService,
    val usersAuthoritiesService: UsersAuthoritiesService,
    val passwordGenerator: RandomValueStringGenerator,
    val passwordEncoder: PasswordEncoder,
    val facebookApi: FacebookApi,
    val githubApi: GitHubApi
) : DefaultOAuth2UserService() {

    init {
        val oAuth2UserRequestEntityConverter = object : OAuth2UserRequestEntityConverter() {
            override fun convert(userRequest: OAuth2UserRequest): RequestEntity<*>? {
                println("oAuth2UserRequestEntityConverter convert: ${userRequest.clientRegistration.registrationId}")
                if (userRequest.clientRegistration.registrationId == GpConstants.SocialProvider.VK.name.toLowerCase()) {
//                    val request = super.convert(userRequest)
//                    request.headers["Authorization"] = "Bearer ${userRequest.accessToken}"
//                    println("oAuth2UserRequestEntityConverter: ${request.headers.entries}")
//                    request.url.query.plus()

                    val clientRegistration = userRequest.clientRegistration

                    val httpMethod = HttpMethod.GET
//                    if (AuthenticationMethod.FORM == clientRegistration.providerDetails.userInfoEndpoint.authenticationMethod) {
//                        httpMethod = HttpMethod.POST
//                    }
                    val headers = HttpHeaders()
                    headers.accept = listOf(MediaType.APPLICATION_JSON)
                    val uri =
                        UriComponentsBuilder.fromUriString(clientRegistration.providerDetails.userInfoEndpoint.uri)
//                            .query("access_token")
                            .queryParam(OAuth2ParameterNames.ACCESS_TOKEN, userRequest.accessToken.tokenValue)
                            .build()
                            .toUri()

//                    val request: RequestEntity<*>
//                        headers.contentType = MediaType.valueOf(APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8");
//                        val formParameters = LinkedMultiValueMap<String, String>()
//                        formParameters.add(OAuth2ParameterNames.ACCESS_TOKEN, userRequest.accessToken.tokenValue)
                    val request = RequestEntity<MultiValueMap<String, String>>(headers, httpMethod, uri)


//                    val request = RequestEntity(formParameters, headers, httpMethod, uri)
                    return request
                } else {
                    return super.convert(userRequest)
                }
            }
        }
        setRequestEntityConverter(oAuth2UserRequestEntityConverter)
//        val PARAMETERIZED_RESPONSE_TYPE = object : ParameterizedTypeReference<Map<String, String>>() {}
//        val restTemplate = object : RestTemplate() {
//            override fun <T : Any?> exchange(
//                requestEntity: RequestEntity<*>,
//                responseType: Class<T>
//            ): ResponseEntity<T> {
//                println("RestTemplate 0: ${requestEntity.url}")
//                val result = super.exchange(requestEntity, responseType)
//                println("result: ${result.body}")
//                return result
//            }
//
//            override fun <T : Any?> doExecute(
//                url: URI,
//                method: HttpMethod?,
//                requestCallback: RequestCallback?,
//                responseExtractor: ResponseExtractor<T>?
//            ): T? {
//                return super.doExecute(url, method, requestCallback, responseExtractor)
//            }
//        }
//        restTemplate.errorHandler = OAuth2ErrorResponseErrorHandler()
//        setRestOperations(restTemplate)
    }

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

        val params = userRequest.additionalParameters

        val oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
            userRequest.clientRegistration.registrationId,
            if (userRequest.clientRegistration.registrationId == GpConstants.SocialProvider.VK.name.toLowerCase()) params else user.attributes,
            userRequest.accessToken?.tokenValue
        )

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
                GITHUB -> {
                    //nothing to do
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
            throw OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider")
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
