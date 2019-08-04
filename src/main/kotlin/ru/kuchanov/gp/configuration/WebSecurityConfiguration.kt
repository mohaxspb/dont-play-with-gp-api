package ru.kuchanov.gp.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.converter.Converter
import org.springframework.http.converter.FormHttpMessageConverter
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
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.savedrequest.SavedRequest
import org.springframework.util.Base64Utils
import org.springframework.web.client.RestTemplate
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.filter.GpOAuth2AuthenticationProcessingFilter
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.network.GitHubApi
import ru.kuchanov.gp.service.auth.GpClientDetailsServiceImpl
import ru.kuchanov.gp.service.auth.GpUserDetailsServiceImpl
import javax.servlet.Filter


@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true
)
class WebSecurityConfiguration @Autowired constructor(
    val userDetailsService: GpUserDetailsServiceImpl,
    val facebookApi: FacebookApi,
    val githubApi: GitHubApi
) : WebSecurityConfigurerAdapter() {

    @Value("\${spring.security.oauth2.client.registration.facebook.clientId}")
    private lateinit var facebookClientId: String
    @Value("\${spring.security.oauth2.client.registration.facebook.clientSecret}")
    private lateinit var facebookClientSecret: String

    @Value("\${spring.security.oauth2.client.registration.github.clientId}")
    private lateinit var githubClientId: String
    @Value("\${spring.security.oauth2.client.registration.github.clientSecret}")
    private lateinit var githubClientSecret: String

    //do not move to constructor - there are circular dependency error
    @Autowired
    lateinit var gpClientDetailsService: GpClientDetailsServiceImpl

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
            .hasAnyAuthority("ADMIN", "USER")

        http
            .formLogin()
            .successHandler { request, response, _ ->
                println("formLogin successHandler: $request")
                val savedRequest = request
                    ?.getSession(false)
                    ?.getAttribute("SPRING_SECURITY_SAVED_REQUEST")as? SavedRequest
                DefaultRedirectStrategy()
                    .sendRedirect(
                        request,
                        response,
                        savedRequest?.let { savedRequest.redirectUrl }
                            ?: "${request.scheme}://${request.serverName}:$angularServerPort$angularServerHref"
                    )
            }
            .and()
            .logout()
            //todo move to separate class
            .addLogoutHandler { _, _, authentication ->
                //logout from providers
                val gpUser = authentication?.principal as? GpUser ?: return@addLogoutHandler

                gpUser.facebookId?.let {
                    val facebookLogoutResult =
                        facebookApi
                            .logout(
                                it,
                                "$facebookClientId|$facebookClientSecret"
                            )
                            //todo no internet handle.
                            .execute()
                            .body()

                    println("facebookLogoutResult: $facebookLogoutResult")
                }
                // nothing to do for google
                // nothing to do for vk
                gpUser.githubToken?.let {
                    val authorization =
                        "Basic " + String(Base64Utils.encode("$githubClientId:$githubClientSecret".toByteArray()))
                    val githubLogoutResult = githubApi.logout(
                        authorization,
                        githubClientId,
                        it
                    )
                        .execute()

                    println("githubLogoutResult: $githubLogoutResult")
                }

                //also clear accessToken in DB
                userDetailsService.insert(
                    gpUser.apply {
                        facebookToken = null
                        vkToken = null
                        googleToken = null
                        githubToken = null
                    })
            }
            .permitAll()
            .logoutSuccessHandler { request, response, _ ->
                DefaultRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "${request.scheme}://${request.serverName}$angularServerPort:$angularServerHref"
                )
            }
            .permitAll()

        val accessTokenResponseClient = DefaultAuthorizationCodeTokenResponseClient()
        /**
         * you can override [fun convert(it: OAuth2AuthorizationCodeGrantRequest): RequestEntity<*>?]
         * and use [requestEntity.body as? MultiValueMap<String, String>] to add params to access_token by auth_code request
         */
        val oAuth2AuthorizationCodeGrantRequestEntityConverter = OAuth2AuthorizationCodeGrantRequestEntityConverter()
        accessTokenResponseClient.setRequestEntityConverter(oAuth2AuthorizationCodeGrantRequestEntityConverter)

        //todo use only for VK
        val tokenResponseConverter = object : Converter<Map<String, String>, OAuth2AccessTokenResponse> {
            override fun convert(source: Map<String, String>): OAuth2AccessTokenResponse? {
                println("tokenResponseConverter convert: $source")

                val vkAccessToken = source[OAuth2ParameterNames.ACCESS_TOKEN]
                val params = mutableMapOf<String, Any?>()
                params["email"] = source["email"] as String
                params["id"] = source["user_id"] as String

                return OAuth2AccessTokenResponse
                    .withToken(vkAccessToken)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .additionalParameters(params)
                    .build()
            }
        }

        val oAuth2AccessTokenResponseHttpMessageConverter = OAuth2AccessTokenResponseHttpMessageConverter()
        oAuth2AccessTokenResponseHttpMessageConverter.setTokenResponseConverter(tokenResponseConverter)

        val restTemplate =
            RestTemplate(listOf(FormHttpMessageConverter(), oAuth2AccessTokenResponseHttpMessageConverter))
        restTemplate.errorHandler = OAuth2ErrorResponseErrorHandler()

        accessTokenResponseClient.setRestOperations(restTemplate)

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

        //filter to allow both access_token auth and cookie auth
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
