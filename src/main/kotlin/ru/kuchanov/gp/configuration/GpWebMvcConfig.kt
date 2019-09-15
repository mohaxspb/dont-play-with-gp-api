package ru.kuchanov.gp.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class GpWebMvcConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/**")
            .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH", "OPTIONS")
            .allowedOrigins(
                "http://localhost:4200",
                "http://localhost:80",
                "http://localhost:443",
                //my dev IP
                "http://192.168.43.235:4200",
                "http://kuchanov.ru",
                "https://kuchanov.ru"
                //todo add prod domains (both http and https to prevent cors problems)
            )
            .allowCredentials(true)
    }
}
