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
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.GpConstants.TARGET_URL_PARAMETER
import ru.kuchanov.gp.bean.auth.AuthorityType
import ru.kuchanov.gp.filter.GpOAuth2AuthenticationProcessingFilter
import ru.kuchanov.gp.service.auth.GpClientDetailsService
import ru.kuchanov.gp.service.auth.GpUserDetailsService
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest


@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true
)
class WebSecurityConfiguration @Autowired constructor(
    val userDetailsService: GpUserDetailsService,
    val logoutHandler: LogoutHandler,
    val formLoginSuccessHandler: GpFromLoginSuccessHandler
) : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var accessTokenResponseClient: OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest>
    //social auth END

    //do not move to constructor - there are circular dependency error
    @Autowired
    lateinit var gpClientDetailsService: GpClientDetailsService

    @Autowired
    private lateinit var defaultOAuth2UserService: DefaultOAuth2UserService

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
            setClientDetailsService(gpClientDetailsService)
            setAuthenticationManager(authenticationManager())
        }

    @Bean
    fun passwordEncoder(): PasswordEncoder =
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
        OAuth2AuthenticationManager()
            .apply {
                setClientDetailsService(gpClientDetailsService)
                setTokenServices(tokenServices())
            }

    /**
     * filter to allow both access_token auth and cookie auth
     */
    @Bean
    fun myOAuth2Filter(): Filter =
        GpOAuth2AuthenticationProcessingFilter()
            .apply {
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
                "/error",
                "/users/",
                "/login**",
                "/oauth/token**",
                "/auth/**",
                "/oauth2/**"
            )
            .permitAll()
            .anyRequest()
            .hasAnyAuthority(*AuthorityType.values().map { it.name }.toTypedArray())

        http
            .formLogin()
            .successHandler(
                formLoginSuccessHandler.apply {
                    setTargetUrlParameter(TARGET_URL_PARAMETER)
                }
            )
            .and()
            .logout()
            .addLogoutHandler(logoutHandler)
            .permitAll()
            .logoutSuccessHandler { request, response, _ ->
                //todo handling for angular without redirection
                DefaultRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "${request.scheme}://${request.serverName}:$angularServerPort$angularServerHref"
                )
            }
            .permitAll()

        http
            .oauth2Login()
            .tokenEndpoint()
            .accessTokenResponseClient(accessTokenResponseClient)
            .and()
            .authorizationEndpoint()
            .baseUri("/oauth2/authorize")
            //if you need to add parameter to auth request (providers site for get auth code)
            //you can override OAuth2AuthorizationRequestResolver and add params to OAuth2AuthorizationRequest.additionalParameters
            //see https://docs.spring.io/spring-security/site/docs/5.1.1.RELEASE/reference/htmlsingle/#oauth2Client-authorization-request-resolver
            //see [authorizationRequestResolver(resolver: OAuth2AuthorizationRequestResolver)]
            .and()
            .redirectionEndpoint()
            .baseUri("/oauth2/callback/*")
            .and()
            .userInfoEndpoint()
            .userService(defaultOAuth2UserService)
            .and()
            .successHandler { request, response, _ ->
                //todo handle for angular without redirection
                DefaultRedirectStrategy()
                    .sendRedirect(
                        request,
                        response,
                        "${request.scheme}://${request.serverName}:$angularServerPort$angularServerHref"
                    )
            }

        http
            .addFilterBefore(
                myOAuth2Filter(),
                BasicAuthenticationFilter::class.java
            )
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(
            "/api/${GpConstants.Path.AUTH}/**"
        )
    }
}
