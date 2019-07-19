package ru.kuchanov.gp

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit4.SpringRunner
import ru.kuchanov.gp.bean.auth.ClientNotFoundError
import ru.kuchanov.gp.bean.auth.OAuthClientDetails
import ru.kuchanov.gp.repository.auth.ClientDetailsRepository
import ru.kuchanov.gp.service.auth.GpClientDetailsService

@RunWith(SpringRunner::class)
@SpringBootTest
class AuthIntegrationTest {

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var clientDetailsRepository: ClientDetailsRepository

    @Autowired
    lateinit var gpClientDetailsService: GpClientDetailsService

    @Before
    fun configure() {
        val oAuthClientDetails = OAuthClientDetails(
            clientId = "test_client_id",
            resource_ids = "",
            client_secret = passwordEncoder.encode("test_client_secret"),
            scope = "read,write",
            authorized_grant_types = "client_credentials,password,refresh_token",
            web_server_redirect_uri = "",
            authorities = "USER,ADMIN",
            access_token_validity = 3600,
            refresh_token_validity = 0,
            additional_information = "",
            autoapprove = "true"
        )
        clientDetailsRepository.save(oAuthClientDetails)
    }

    @Test
    fun clientDetailsRepository_insertWorks() {
        val authorities = "USER,ADMIN"

        val clientId = "test_client_id"
        val foundClient = clientDetailsRepository.getOne(clientId)

        assertThat(foundClient.authorities).isEqualTo(authorities)
    }

    @Test
    fun clientDetailsService_insertWorks() {
        val clientId = "test_client_id"
        val foundClient = gpClientDetailsService.loadClientByClientId(clientId)

        assertThat(foundClient.clientId).isEqualTo(clientId)
    }

    @Test(expected = ClientNotFoundError::class)
    fun clientDetailsService_notFoundThrowsError() {
        val clientId = "unexisting_client_id"
        gpClientDetailsService.loadClientByClientId(clientId)
    }
}
