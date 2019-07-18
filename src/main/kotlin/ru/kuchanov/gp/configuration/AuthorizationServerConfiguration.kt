package ru.kuchanov.gp.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore
import ru.kuchanov.gp.service.auth.ClientServiceImpl
import ru.kuchanov.gp.service.auth.UserServiceImpl
import javax.sql.DataSource


@Configuration
@EnableAuthorizationServer
class AuthorizationServerConfiguration @Autowired constructor(
    val clientDetailsService: ClientServiceImpl,
    val userDetailsService: UserServiceImpl
) : AuthorizationServerConfigurerAdapter() {

    //do not move to constructor - there are circular dependency error
    @Autowired
    private lateinit var dataSource: DataSource

    //do not move to constructor - there are circular dependency error
    @Autowired
    lateinit var authenticationManager: AuthenticationManager

    @Bean
    fun tokenStore(): TokenStore = JdbcTokenStore(dataSource)

    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.withClientDetails(clientDetailsService)
    }

    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints
            .prefix("/api")
            .tokenStore(tokenStore())
            .authenticationManager(authenticationManager)
            .userDetailsService(userDetailsService)
    }
}
