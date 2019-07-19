package ru.kuchanov.gp

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner
import ru.kuchanov.gp.bean.auth.OAuthClientDetails
import ru.kuchanov.gp.repository.auth.ClientDetailsRepository

@RunWith(SpringRunner::class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthIntegrationTest {

    @Autowired
    lateinit var clientDetailsRepository: ClientDetailsRepository

    @Before
    fun configure() {
        val oAuthClientDetails = OAuthClientDetails(
            client_id = "test_client_id",
            resource_ids = "",
            client_secret = "test_client_secret",
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
    fun clientDetails_insertWorks() {
        val authorities = "USER,ADMIN"

        val clientId = "test_client_id"
        val foundClient = clientDetailsRepository.getOne(clientId)

        assertThat(foundClient.authorities).isEqualTo(authorities)
    }
}
