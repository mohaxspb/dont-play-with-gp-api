package ru.kuchanov.gp.configuration

import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import ru.kuchanov.gp.network.FacebookApi
import ru.kuchanov.gp.network.GitHubApi
import java.security.SecureRandom

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
    private lateinit var converterFactory: Converter.Factory
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

}
