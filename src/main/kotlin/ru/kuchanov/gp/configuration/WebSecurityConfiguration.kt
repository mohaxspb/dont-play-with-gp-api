package ru.kuchanov.gp.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.service.auth.ClientServiceImpl
import ru.kuchanov.gp.service.auth.UserServiceImpl
import javax.servlet.Filter


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
class WebSecurityConfiguration @Autowired constructor(
    var userDetailsService: UserServiceImpl
) : WebSecurityConfigurerAdapter() {

    //do not move to constructor - there are circular dependency error
    @Autowired
    lateinit var clientDetailsService: ClientServiceImpl

    //do not move to constructor - there are circular dependency error
    @Autowired
    lateinit var tokenStore: TokenStore

    @Autowired
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth
            .authenticationProvider(authenticationProvider())
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder())
    }

    @Bean
    fun tokenServices() =
        DefaultTokenServices().apply {
            setTokenStore(tokenStore)
            setClientDetailsService(clientDetailsService)
            setAuthenticationManager(authenticationManager())
        }

    @Bean
    fun passwordEncoder() =
        BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider =
        DaoAuthenticationProvider().apply {
            setUserDetailsService(userDetailsService)
            setPasswordEncoder(passwordEncoder())
        }

    @Primary
    @Bean
    override fun authenticationManagerBean(): AuthenticationManager =
        super.authenticationManagerBean()

    @Bean
    fun oauth2authenticationManager(): OAuth2AuthenticationManager =
        OAuth2AuthenticationManager().apply {
            setClientDetailsService(clientDetailsService)
            setTokenServices(tokenServices())
        }

    @Bean
    fun myOAuth2Filter(): Filter =
        OAuth2AuthenticationProcessingFilter().apply {
            setAuthenticationManager(oauth2authenticationManager())
            //allow auth with cookies (not only with access_token)
            setStateless(false)
        }

    @Value("\${angular.port}")
    lateinit var angularServerPort: String

    @Value("\${angular.href}")
    lateinit var angularServerHref: String

    override fun configure(http: HttpSecurity) {
        http
            .cors()
        http
            .csrf()
            .disable()

        //rules for $server.servlet.context-path
        //allow index and users controllers "/" paths
        //require authorities for any other requests
        http
            .authorizeRequests()
            .antMatchers(
                "/",
                "/users/"
            )
            .permitAll()
            .anyRequest()
            .hasAnyAuthority("ADMIN", "USER")

        http
            .formLogin()
            .successHandler { request, response, _ ->
                DefaultRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "${request.scheme}://${request.serverName}$angularServerPort$angularServerHref"
                )
            }
            .and()
            .logout()
            .logoutSuccessHandler { request, response, _ ->
                DefaultRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "${request.scheme}://${request.serverName}$angularServerPort$angularServerHref"
                )
            }
            .permitAll()

        //filter to allow both access_token auth and cookie auth
        http
            .addFilterBefore(
                myOAuth2Filter(),
                BasicAuthenticationFilter::class.java
            )
    }

    override fun configure(web: WebSecurity) {
        web.ignoring()
            .antMatchers(
                "/${GpConstants.Path.AUTH}/**"
            )
    }
}
