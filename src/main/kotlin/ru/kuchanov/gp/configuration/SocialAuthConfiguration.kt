package ru.kuchanov.gp.configuration

import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.web.client.RestTemplate
import retrofit2.CallAdapter
import retrofit2.Retrofit
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.network.GitHubApi
import java.security.SecureRandom
import retrofit2.Converter as RetrofitConverter

@Configuration
class SocialAuthConfiguration {

    @Bean
    fun passwordGenerator(): RandomValueStringGenerator =
        RandomValueStringGenerator().apply {
            setLength(10)
            setRandom(SecureRandom())
        }

    //facebook
    @Autowired
    private lateinit var okHttpClient: OkHttpClient
    @Autowired
    private lateinit var converterFactory: RetrofitConverter.Factory
    @Autowired
    private lateinit var callAdapterFactory: CallAdapter.Factory

    private fun facebookRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(FacebookApi.BASE_API_URL)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .addCallAdapterFactory(callAdapterFactory)
        .build()

    @Bean
    fun facebookApi(): FacebookApi = facebookRetrofit().create(FacebookApi::class.java)

    //github
    private fun githubRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(GitHubApi.BASE_API_URL)
        .client(okHttpClient)
        .addConverterFactory(converterFactory)
        .addCallAdapterFactory(callAdapterFactory)
        .build()

    @Bean
    fun gitHubApi(): GitHubApi = githubRetrofit().create(GitHubApi::class.java)

    //vk

    /**
     * we need it, as vk returns email with access_token, so we must pass it to additional params
     */
    @Bean
    fun socialTokenResponseConverter() =
        Converter<Map<String, String>, OAuth2AccessTokenResponse> { source ->
            println("tokenResponseConverter convert: $source")

            val vkAccessToken = source[OAuth2ParameterNames.ACCESS_TOKEN]
            val params = mutableMapOf<String, Any?>()
            params["email"] = source["email"]
            params["id"] = source["user_id"]

            OAuth2AccessTokenResponse
                .withToken(vkAccessToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .additionalParameters(params)
                .build()
        }

    @Bean
    fun accessTokenResponseClient(): OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> =
        DefaultAuthorizationCodeTokenResponseClient().apply {
            /**
             * you can override [fun convert(it: OAuth2AuthorizationCodeGrantRequest): RequestEntity<*>?]
             * and use [requestEntity.body as? MultiValueMap<String, String>] to add params to access_token by auth_code request
             */
            val oAuth2AuthCodeGrantRequestEntityConverter = OAuth2AuthorizationCodeGrantRequestEntityConverter()
            setRequestEntityConverter(oAuth2AuthCodeGrantRequestEntityConverter)

            val oAuth2AccessTokenResponseHttpMessageConverter = OAuth2AccessTokenResponseHttpMessageConverter()
            oAuth2AccessTokenResponseHttpMessageConverter.setTokenResponseConverter(socialTokenResponseConverter())
            val restTemplate =
                RestTemplate(
                    listOf(
                        FormHttpMessageConverter(),
                        oAuth2AccessTokenResponseHttpMessageConverter
                    )
                )
            restTemplate.errorHandler = OAuth2ErrorResponseErrorHandler()
            setRestOperations(restTemplate)
        }
}
