package ru.kuchanov.gp.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Converter
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

@Configuration
class NetworkConfiguration {

    //okHttp + retrofit
    @Bean
    fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor(
            object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    println("OkHttp: $message")
                }
            }
        )
            .setLevel(HttpLoggingInterceptor.Level.BODY)

    @Bean
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor())
        .connectTimeout(OK_HTTP_CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(OK_HTTP_READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(OK_HTTP_WRITE_TIMEOUT, TimeUnit.SECONDS)
        .build()

    @Bean
    fun callAdapterFactory(): RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

    @Bean
    fun converterFactory(): Converter.Factory = JacksonConverterFactory.create(objectMapper())
    //okHttp + retrofit END

    companion object {
        const val OK_HTTP_CONNECT_TIMEOUT = 30L
        const val OK_HTTP_READ_TIMEOUT = 30L
        const val OK_HTTP_WRITE_TIMEOUT = 30L
    }
}
