package ru.kuchanov.gp

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.common.OAuth2AccessToken.REFRESH_TOKEN
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import ru.kuchanov.gp.bean.auth.*
import ru.kuchanov.gp.repository.auth.ClientDetailsRepository
import ru.kuchanov.gp.service.auth.AuthService
import ru.kuchanov.gp.service.auth.GpClientDetailsService
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import ru.kuchanov.gp.service.auth.UsersAuthoritiesService
import org.springframework.util.Base64Utils




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
    lateinit var mvc: MockMvc

    @Before
    fun configure() {
        val oAuthClientDetails = OAuthClientDetails(
            clientId = TEST_CLIENT_ID,
            resource_ids = "",
            client_secret = passwordEncoder.encode(TEST_CLIENT_SECRET),
            scope = "read,write",
            authorized_grant_types = "client_credentials,password,$REFRESH_TOKEN",
            web_server_redirect_uri = "",
            authorities = "USER,ADMIN",
            access_token_validity = 3600,
            refresh_token_validity = 0,
            additional_information = "",
            autoapprove = "true"
        )
        clientDetailsRepository.save(oAuthClientDetails)


        val inDbUser = userDetailsService.loadUserByUsername(TEST_USERNAME) ?: userDetailsService.insert(
            GpUser(
                username = TEST_USERNAME,
                password = passwordEncoder.encode(TEST_USERNAME)
            )
        )

        if (usersAuthorityService.findByUserIdAndAuthority(inDbUser.id!!, AuthorityType.USER).isNullOrEmpty()) {
            usersAuthorityService.insert(
                UsersAuthorities(
                    userId = inDbUser.id!!, authority = AuthorityType.USER
                )
            )
        }
    }

    @Test
    fun clientDetailsRepository_insertWorks() {
        val authorities = "USER,ADMIN"

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
        val accessToken = authService.getAccessTokenForUsernameAndClientId(TEST_USERNAME, TEST_CLIENT_ID)

        println("accessToken: $accessToken")

        val accessTokenAsJson = objectMapper.writeValueAsString(accessToken)

        mvc.perform(
            post("/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(
                    AUTHORIZATION,
                    "Basic " + String(Base64Utils.encode("$TEST_CLIENT_ID:$TEST_CLIENT_SECRET".toByteArray()))
                )
                .param("grant_type", "password")
                .param("username", TEST_USERNAME)
                .param("password", TEST_USERNAME)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
            .andExpect(content().json(accessTokenAsJson))
    }

    companion object {
        const val TEST_USERNAME = "test@test.ru"
        const val TEST_CLIENT_ID = "test_client_id"
        const val TEST_CLIENT_SECRET = "test_client_secret"
    }
}
