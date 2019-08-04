package ru.kuchanov.gp.configuration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
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
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager
import org.springframework.security.oauth2.provider.token.DefaultTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.security.web.savedrequest.SavedRequest
import org.springframework.util.Base64Utils
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestTemplate
import ru.kuchanov.gp.GpConstants
import ru.kuchanov.gp.bean.auth.GpUser
import ru.kuchanov.gp.filter.GpOAuth2AuthenticationProcessingFilter
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.network.GitHubApi
import ru.kuchanov.gp.service.auth.GpClientDetailsServiceImpl
import ru.kuchanov.gp.service.auth.GpUserDetailsServiceImpl
import java.net.URI
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest
import kotlin.collections.LinkedHashMap


@Configuration
@EnableWebSecurity
@EnableOAuth2Client
@EnableGlobalMethodSecurity(
    prePostEnabled = true,
    securedEnabled = true
)
class WebSecurityConfiguration @Autowired constructor(
    val clientRegistrationRepository: ClientRegistrationRepository,
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
                val gpUser = authentication.principal as? GpUser ?: return@addLogoutHandler

                gpUser.facebookId?.let {
                    val facebookLogoutResult =
                        facebookApi
                            .logout(
                                it,
                                "$facebookClientId|$facebookClientSecret"
                            )
                            .execute()
                            .body()

                    println("facebookLogoutResult: $facebookLogoutResult")
                }
                // nothing to do for google
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

                gpUser.vkId?.let {
                    TODO()
                }

                //also clear accessToken in DB
                userDetailsService.insert(gpUser.apply {
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
//        accessTokenResponseClient.setRequestEntityConverter {
//            println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationRequest.authorizationRequestUri}")
//            println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationRequest.additionalParameters.entries}")
//            println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationRequest.clientId}")
//            println("DefaultAuthorizationCodeTokenResponseClient: ${it.clientRegistration}")
//            println("DefaultAuthorizationCodeTokenResponseClient: ${it.grantType.value}")
//            println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationResponse.code}")
//            println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationResponse.error}")
//            return@setRequestEntityConverter null
//        }
        val oAuth2AuthorizationCodeGrantRequestEntityConverter = object :OAuth2AuthorizationCodeGrantRequestEntityConverter(){
            override fun convert(it: OAuth2AuthorizationCodeGrantRequest): RequestEntity<*>? {
                println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationRequest.authorizationRequestUri}")
                println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationRequest.additionalParameters.entries}")
                println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationRequest.clientId}")
                println("DefaultAuthorizationCodeTokenResponseClient: ${it.clientRegistration}")
                println("DefaultAuthorizationCodeTokenResponseClient: ${it.grantType.value}")
                println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationResponse.code}")
                println("DefaultAuthorizationCodeTokenResponseClient: ${it.authorizationExchange.authorizationResponse.error}")
                val requestEntity = super.convert(it)
                println("requestEntity: ${requestEntity.url}")
                println("requestEntity: ${requestEntity.headers}")
                println("requestEntity body: ${requestEntity.body}")
                println("requestEntity: ${requestEntity.type?.typeName}")
                (requestEntity.body as? MultiValueMap<String, String>)?.apply {
                    set(OAuth2ParameterNames.CLIENT_ID, it.clientRegistration.clientId)
                    set(OAuth2ParameterNames.CLIENT_SECRET, it.clientRegistration.clientSecret)
                }
                println("requestEntity body: ${requestEntity.body}")
                return requestEntity
            }
        }
        accessTokenResponseClient.setRequestEntityConverter(oAuth2AuthorizationCodeGrantRequestEntityConverter)

        val oAuth2AccessTokenResponseHttpMessageConverter = object :OAuth2AccessTokenResponseHttpMessageConverter(){
            override fun readInternal(
                clazz: Class<out OAuth2AccessTokenResponse>,
                inputMessage: HttpInputMessage
            ): OAuth2AccessTokenResponse {

               val PARAMETERIZED_RESPONSE_TYPE =  object : ParameterizedTypeReference<Map<String, String>>() {}
                val tokenResponseParameters = MappingJackson2HttpMessageConverter().read(
                    PARAMETERIZED_RESPONSE_TYPE.type, null, inputMessage
                ) as Map<String, String>

                println("OAuth2AccessTokenResponseHttpMessageConverter: ${tokenResponseParameters.entries}.")

                val vkAccessToken = tokenResponseParameters[OAuth2ParameterNames.ACCESS_TOKEN]
                println("vkAccessToken: $vkAccessToken")
//                val params = mutableMapOf<String, Any>()
//                params.set("email", )
//                val params = mutableMapOf<String, Any>()

                val oAuth2AccessTokenResponse = OAuth2AccessTokenResponse
                    .withToken(vkAccessToken)
                    .tokenType(OAuth2AccessToken.TokenType.BEARER)
                    .additionalParameters(tokenResponseParameters)
                    .build()
                println("token: $oAuth2AccessTokenResponse")

                return oAuth2AccessTokenResponse

//                return super.readInternal(clazz, inputMessage)
            }
        }
//val OAuth2AccessTokenResponseConverter = OAuth2AccessTokenResponseConverter()
//        oAuth2AccessTokenResponseHttpMessageConverter.setTokenResponseConverter()
        val restTemplate = object :RestTemplate(
            listOf(
                FormHttpMessageConverter(), oAuth2AccessTokenResponseHttpMessageConverter
            )
        ){
            override fun <T : Any?> exchange(
                requestEntity: RequestEntity<*>,
                responseType: Class<T>
            ): ResponseEntity<T> {
                println("RestTemplate 0: ${requestEntity.url}")
                println("RestTemplate 0: ${(requestEntity.body as Map<String, String>).entries}")

                val result = super.exchange(requestEntity, responseType)
                val token = (result.body as OAuth2AccessTokenResponse)
                println("RestTemplate 0 result: ${token.accessToken.tokenValue}")
                println("RestTemplate 0 result: ${token.additionalParameters.entries}")

                val params = mutableMapOf<String, Any?>()
                params.set("email", token.additionalParameters["email"])
                params.set("id", token.additionalParameters["user_id"])
                //todo
                params.set("name", "test")
                //todo
                params.set("picture", null)

//                val responseEntity = super.exchange(url, method, requestEntity, responseType)
//                println("responseEntity: $responseEntity")
//                return responseEntity

//                return result

                val myToken:T = OAuth2AccessTokenResponse
                    .withResponse(token)
                    .additionalParameters(params)
                    .build() as T

//                return ResponseEntity(myToken as T, result.headers, result.statusCodeValue)
//                return ResponseEntity.ok(myToken)

                val newResult = ResponseEntity.status(result.statusCode)
                    .body(myToken)
                println("newResult: ${(newResult.body as OAuth2AccessTokenResponse ).additionalParameters.entries}")
                return newResult
            }

            override fun <T : Any?> doExecute(
                url: URI,
                method: HttpMethod?,
                requestCallback: RequestCallback?,
                responseExtractor: ResponseExtractor<T>?
            ): T? {
                return super.doExecute(url, method, requestCallback, responseExtractor)
            }
        }
        restTemplate.errorHandler = OAuth2ErrorResponseErrorHandler()

        accessTokenResponseClient.setRestOperations(restTemplate)

        http
            .oauth2Login()
            .tokenEndpoint()
            .accessTokenResponseClient(accessTokenResponseClient)
            .and()
            .authorizationEndpoint()
            .baseUri("/oauth2/authorize")
            //fixme remove it
            .authorizationRequestResolver(object : OAuth2AuthorizationRequestResolver {
                val defaultOAuth2AuthorizationRequestResolver = DefaultOAuth2AuthorizationRequestResolver(
                    clientRegistrationRepository,
                    "/oauth2/authorize"
                )

                override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest? {
                    println("OAuth2AuthorizationRequestResolver resolve: $request")
                    val authorizationRequest = defaultOAuth2AuthorizationRequestResolver.resolve(request)
                    return authorizationRequest?.let {
                        customAuthorizationRequest(authorizationRequest)
                    }
                }

                override fun resolve(
                    request: HttpServletRequest?,
                    clientRegistrationId: String?
                ): OAuth2AuthorizationRequest? {
                    println("OAuth2AuthorizationRequestResolver resolve: $request $clientRegistrationId")
                    val authorizationRequest = defaultOAuth2AuthorizationRequestResolver.resolve(request)
                    return authorizationRequest?.let {
                        customAuthorizationRequest(authorizationRequest)
                    }
                }
            })
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

    private fun customAuthorizationRequest(authorizationRequest: OAuth2AuthorizationRequest): OAuth2AuthorizationRequest {

        val additionalParameters: MutableMap<String, Any>? = LinkedHashMap(authorizationRequest.additionalParameters)
        additionalParameters?.set("prompt", "consent")

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters(additionalParameters)
            .build();
    }

    override fun configure(web: WebSecurity) {
        web.ignoring().antMatchers(
            "/api/${GpConstants.Path.AUTH}/**"
        )
    }
}
