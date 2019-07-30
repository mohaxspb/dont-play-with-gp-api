package ru.kuchanov.gp.configuration

//import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
//import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.jackson2.JacksonFactory
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import ru.kuchanov.gp.network.FacebookApi
import java.security.SecureRandom

@Configuration
class SocialAuthConfiguration {

    @Bean
    fun passwordGenerator(): RandomValueStringGenerator =
            RandomValueStringGenerator().apply {
                setLength(10)
                setRandom(SecureRandom())
            }

    //google auth
//    @Value("\${my.api.admin.google.client_id}")
//    private lateinit var googleClientIdAdmin: String
//    @Value("\${my.api.game.google.client_id}")
//    private lateinit var googleClientIdGame: String

//    @Bean
//    fun googleIdTokenVerifier(): GoogleIdTokenVerifier =
//            GoogleIdTokenVerifier.Builder(NetHttpTransport(), JacksonFactory())
//                    .setAudience(listOf(googleClientIdAdmin, googleClientIdGame))
//                    .build()

    //facebook
    @Autowired
    private lateinit var okHttpClient: OkHttpClient
    @Autowired
    private lateinit var converterFactory: Converter.Factory
    @Autowired
    private lateinit var callAdapterFactory: CallAdapter.Factory

    @Bean
    fun retrofit(): Retrofit = Retrofit.Builder()
            .baseUrl(FacebookApi.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .addCallAdapterFactory(callAdapterFactory)
            .build()

    @Bean
    fun facebookApi(): FacebookApi = retrofit().create(FacebookApi::class.java)
}
