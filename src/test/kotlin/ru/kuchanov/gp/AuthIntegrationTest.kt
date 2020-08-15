package ru.kuchanov.gp

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.common.OAuth2AccessToken
import org.springframework.security.oauth2.common.OAuth2AccessToken.REFRESH_TOKEN
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.util.Base64Utils
import ru.kuchanov.gp.bean.auth.*
import ru.kuchanov.gp.repository.auth.ClientDetailsRepository
import ru.kuchanov.gp.service.auth.AuthService
import ru.kuchanov.gp.service.auth.GpClientDetailsService
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.auth.UsersAuthoritiesService
import ru.kuchanov.gp.service.data.LanguageService


@RunWith(SpringRunner::class)
@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var clientDetailsRepository: ClientDetailsRepository

    @Autowired
    lateinit var gpClientDetailsService: GpClientDetailsService

    @Autowired
    lateinit var userDetailsService: GpUserDetailsService

    @Autowired
    lateinit var usersAuthorityService: UsersAuthoritiesService

    @Autowired
    lateinit var authService: AuthService

    @Autowired
    lateinit var languageService: LanguageService

    @Autowired
    lateinit var mvc: MockMvc

    @Before
    fun configure() {
        val oAuthClientDetails = OAuthClientDetails(
            clientId = TEST_CLIENT_ID,
            resource_ids = "",
            client_secret = passwordEncoder.encode(TEST_CLIENT_SECRET),
            scope = "read,write",
            authorized_grant_types = "client_credentials,password,$REFRESH_TOKEN",
            web_server_redirect_uri = "/test/",
            authorities = "${AuthorityType.USER.name},${AuthorityType.ADMIN.name}",
            access_token_validity = 3600,
            refresh_token_validity = 0,
            additional_information = "",
            autoapprove = "true"
        )
        clientDetailsRepository.save(oAuthClientDetails)

        val oAuthClientDetailsWithFastTokenExpiration = OAuthClientDetails(
            clientId = FAST_TOKEN_EXPIRES_CLIENT_ID,
            resource_ids = "",
            client_secret = passwordEncoder.encode(TEST_CLIENT_SECRET),
            scope = "read,write",
            authorized_grant_types = "client_credentials,password,$REFRESH_TOKEN",
            web_server_redirect_uri = "/test/",
            authorities = "${AuthorityType.USER.name},${AuthorityType.ADMIN.name}",
            access_token_validity = 1,
            refresh_token_validity = 0,
            additional_information = "",
            autoapprove = "true"
        )
        clientDetailsRepository.save(oAuthClientDetailsWithFastTokenExpiration)

        val inDbUser = userDetailsService.loadUserByUsername(TEST_USERNAME) ?: userDetailsService.save(
            GpUser(
                username = TEST_USERNAME,
                password = passwordEncoder.encode(TEST_USERNAME),
                fullName = TEST_FULL_NAME,
                primaryLanguageId = languageService.findByLangCode(GpConstants.DEFAULT_LANG_CODE)?.id!!
            )
        )

        if (usersAuthorityService.findByUserIdAndAuthority(inDbUser.id!!, AuthorityType.USER) == null) {
            usersAuthorityService.save(
                UsersAuthorities(
                    userId = inDbUser.id!!, authority = AuthorityType.USER
                )
            )
        }
    }

    @Test
    fun clientDetailsRepository_insertWorks() {
        val authorities = "${AuthorityType.USER.name},${AuthorityType.ADMIN.name}"

        val clientId = TEST_CLIENT_ID
        val foundClient = clientDetailsRepository.getOne(clientId)

        assertThat(foundClient.authorities).isEqualTo(authorities)
    }

    @Test
    fun clientDetailsService_insertWorks() {
        val clientId = TEST_CLIENT_ID
        val foundClient = gpClientDetailsService.loadClientByClientId(clientId)

        assertThat(foundClient.clientId).isEqualTo(clientId)
    }

    @Test(expected = ClientNotFoundError::class)
    fun clientDetailsService_notFoundThrowsError() {
        val clientId = "unexisting_client_id"
        gpClientDetailsService.loadClientByClientId(clientId)
    }

    @Test
    fun getAccessTokenByPassword_returnsAccessToken() {
        val oauth2Authentication =
            authService.generateAuthenticationForUsernameAndClientId(TEST_USERNAME, TEST_CLIENT_ID)
        val accessToken = authService.getAccessTokenFromAuthentication(oauth2Authentication)

        val accessTokenValue = accessToken.value

        accessTokenRequest(TEST_CLIENT_ID)
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("access_token", `is`(accessTokenValue)))
    }

    @Test
    fun securedUrlWithAccessToken_answersOK() {
        val accessToken = objectMapper.readValue(
            accessTokenRequest(TEST_CLIENT_ID).andReturn().response.contentAsString,
            OAuth2AccessToken::class.java
        )
//        println("accessToken: $accessToken")

        val userJson = objectMapper
            .writeValueAsString(userDetailsService.loadUserByUsername(TEST_USERNAME)
            !!.toDto(includeEmail = true))

        mvc.perform(
            get("/" + GpConstants.UsersEndpoint.PATH + "/" + GpConstants.UsersEndpoint.Method.ME)
                .header(AUTHORIZATION, "Bearer ${accessToken.value}")
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(userJson))
    }

    @Test
    fun securedUrlWithoutAccessToken_redirectsToLogin() {
        mvc.perform(
            get("/" + GpConstants.UsersEndpoint.PATH + "/" + GpConstants.UsersEndpoint.Method.ME)
        )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrlPattern("**/login"))
    }

    @Test
    fun expiredTokenRequest_failsWithUnauthorizedError() {
        val accessToken = objectMapper.readValue(
            accessTokenRequest(FAST_TOKEN_EXPIRES_CLIENT_ID).andReturn().response.contentAsString,
            OAuth2AccessToken::class.java
        )
        Thread.sleep(2000)
        mvc.perform(
            get("/" + GpConstants.UsersEndpoint.PATH + "/" + GpConstants.UsersEndpoint.Method.ME)
                .header(AUTHORIZATION, "Bearer ${accessToken.value}")
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun refreshingExpiredToken_justWorks() {
        val accessToken = objectMapper.readValue(
            accessTokenRequest(FAST_TOKEN_EXPIRES_CLIENT_ID).andReturn().response.contentAsString,
            OAuth2AccessToken::class.java
        )
        Thread.sleep(2000)
        mvc.perform(
            get("/" + GpConstants.UsersEndpoint.PATH + "/" + GpConstants.UsersEndpoint.Method.ME)
                .header(AUTHORIZATION, "Bearer ${accessToken.value}")
        )
            .andDo { println(it.response.contentAsString) }
            .andExpect(status().isUnauthorized)

        val refreshedAccessToken = objectMapper.readValue(
            refreshAccessTokenRequest(accessToken.refreshToken.value).andReturn().response.contentAsString,
            OAuth2AccessToken::class.java
        )
        mvc.perform(
            get("/" + GpConstants.UsersEndpoint.PATH + "/" + GpConstants.UsersEndpoint.Method.ME)
                .header(AUTHORIZATION, "Bearer ${refreshedAccessToken.value}")
        )
            .andDo { println(it.response.contentAsString) }
            .andExpect(status().isOk)
    }

    @Test
    fun registerUser_createsUserAndReturnsAccessToken() {
        val registerResponse = mvc.perform(
            post("/" + GpConstants.AuthEndpoint.PATH + "/" + GpConstants.AuthEndpoint.Method.REGISTER)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", TEST_REGISTER_USERNAME)
                .param("password", TEST_REGISTER_USERNAME)
                .param("fullName", TEST_REGISTER_FULL_NAME)
                .param("primaryLanguage", GpConstants.DEFAULT_LANG_CODE)
                .param("clientId", TEST_CLIENT_ID)
        )
            .andExpect(status().isOk)
            .andReturn().response.contentAsString

        val parsedResponse = objectMapper.readValue(registerResponse, OAuth2AccessToken::class.java)
        @Suppress("UsePropertyAccessSyntax")
        assertThat(parsedResponse.value).isNotEmpty()

        val registeredUser = userDetailsService.loadUserByUsername(TEST_REGISTER_USERNAME)
        assertThat(registeredUser).isNotNull

        val registeredUserAuthorities = usersAuthorityService.findAllByUserId(registeredUser!!.id!!)
        assertThat(registeredUserAuthorities).isNotEmpty
    }

    @After
    fun clearData() {
        //todo clear all, that we insert in DB

        userDetailsService.deleteByUsername(TEST_REGISTER_USERNAME)
    }

    private fun accessTokenRequest(clientId: String) =
        mvc.perform(
            post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                    AUTHORIZATION,
                    "Basic " + String(Base64Utils.encode("$clientId:$TEST_CLIENT_SECRET".toByteArray()))
                )
                .param("grant_type", "password")
                .param("username", TEST_USERNAME)
                .param("password", TEST_USERNAME)
        )

    private fun refreshAccessTokenRequest(refreshToken: String) =
        mvc.perform(
            post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                    AUTHORIZATION,
                    "Basic " + String(Base64Utils.encode("$FAST_TOKEN_EXPIRES_CLIENT_ID:$TEST_CLIENT_SECRET".toByteArray()))
                )
                .param("grant_type", REFRESH_TOKEN)
                .param(REFRESH_TOKEN, refreshToken)
        )

    companion object {
        const val TEST_USERNAME = "test@test.ru"
        const val TEST_FULL_NAME = "test fullName"
        const val TEST_REGISTER_USERNAME = "test-register@test.ru"
        const val TEST_REGISTER_FULL_NAME = "test register fullName"
        const val TEST_CLIENT_ID = "test_client_id"
        const val FAST_TOKEN_EXPIRES_CLIENT_ID = "fast_token_expires_client_id"
        const val TEST_CLIENT_SECRET = "test_client_secret"
    }
}
