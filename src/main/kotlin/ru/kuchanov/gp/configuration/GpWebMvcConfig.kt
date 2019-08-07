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
                        "http://localhost:443"
                        //todo add domains
                )
                .allowedHeaders("*")
                .allowCredentials(true)
    }
}
